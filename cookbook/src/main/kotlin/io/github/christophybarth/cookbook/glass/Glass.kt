/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.glass

import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import android.graphics.RenderEffect as AndroidRenderEffect

/**
 * AGSL fragment that boosts saturation of an input shader. Standard Rec. 709 luma + lerp toward
 * the original colour by `amount`. Applied as a chained `RenderEffect` after the blur on API
 * 33+; without it, Gaussian blur in linear RGB averages colours toward grey and the panel reads
 * as muddy plastic instead of luminous glass.
 */
private const val SATURATION_SHADER_SOURCE = """
    uniform shader content;
    uniform float amount;
    half4 main(vec2 coord) {
        half4 px = content.eval(coord);
        float gray = dot(px.rgb, vec3(0.2126, 0.7152, 0.0722));
        return half4(mix(vec3(gray), px.rgb, amount), px.a);
    }
"""

private const val SATURATION_UNIFORM_AMOUNT = "amount"
private const val SATURATION_SHADER_INPUT_NAME = "content"

/**
 * AGSL fragment that does the entire glass effect in a single pass when chromatic aberration
 * is enabled: 13-tap pseudo-Gaussian blur of the raw layer, Rec.709 saturation lift, and a
 * two-part chromatic rim. Applied to a per-panel offscreen layer ([GlassNode.panelLayer]).
 *
 * Rim is two-part to guarantee visibility regardless of backdrop content:
 *  1. Content refraction: R/G/B sampled from different radial positions (subtle channel split).
 *  2. Additive angular rainbow: hue derived from the pixel's angle around the panel center,
 *     additively blended at the rim; always visible even on uniform-coloured backdrops.
 *
 * 13 blur taps: center (0.20), 4 cardinal at radius (0.10 each), 4 diagonal at ~0.707·radius
 * (0.075 each), 4 cardinal at 1.5·radius (0.025 each). Weights sum to 1.0.
 * `rimMask = smoothstep(bandStart, 1.0, r)` confines both effects to a rim band whose width
 * grows with the slider: `bandStart = mix(0.92, 0.52, norm)` (outer ~8% at 0 dp, ~48% at 64 dp).
 */
private const val GLASS_SHADER_SOURCE = """
    uniform shader content;
    uniform vec2 panelSize;
    uniform float blurRadius;
    uniform float saturation;
    uniform float aberration;
    uniform float hueShift;

    half4 main(vec2 coord) {
        vec2 lo = vec2(0.0);
        vec2 hi = panelSize - vec2(1.0);
        vec2 panelHalf = max(panelSize * 0.5, vec2(1.0));
        vec2 ndc = (coord - panelHalf) / panelHalf;
        float r = clamp(length(ndc), 0.0, 1.0);

        // 13-tap pseudo-Gaussian. Every tap clamped so out-of-bounds coords return a real
        // border pixel instead of transparent (Skia decal mode returns vec4(0) past bounds).
        half4 b = content.eval(clamp(coord, lo, hi)) * 0.20;
        b += content.eval(clamp(coord + vec2( blurRadius,  0.0),                  lo, hi)) * 0.10;
        b += content.eval(clamp(coord - vec2( blurRadius,  0.0),                  lo, hi)) * 0.10;
        b += content.eval(clamp(coord + vec2( 0.0,         blurRadius),            lo, hi)) * 0.10;
        b += content.eval(clamp(coord - vec2( 0.0,         blurRadius),            lo, hi)) * 0.10;
        b += content.eval(clamp(coord + vec2( blurRadius,  blurRadius)  * 0.707,  lo, hi)) * 0.075;
        b += content.eval(clamp(coord + vec2( blurRadius, -blurRadius)  * 0.707,  lo, hi)) * 0.075;
        b += content.eval(clamp(coord + vec2(-blurRadius,  blurRadius)  * 0.707,  lo, hi)) * 0.075;
        b += content.eval(clamp(coord + vec2(-blurRadius, -blurRadius)  * 0.707,  lo, hi)) * 0.075;
        b += content.eval(clamp(coord + vec2( blurRadius * 1.5, 0.0),             lo, hi)) * 0.025;
        b += content.eval(clamp(coord - vec2( blurRadius * 1.5, 0.0),             lo, hi)) * 0.025;
        b += content.eval(clamp(coord + vec2( 0.0,  blurRadius * 1.5),            lo, hi)) * 0.025;
        b += content.eval(clamp(coord - vec2( 0.0,  blurRadius * 1.5),            lo, hi)) * 0.025;

        // Rec.709 saturation lift.
        float gray = dot(b.rgb, vec3(0.2126, 0.7152, 0.0722));
        b = half4(mix(vec3(gray), b.rgb, saturation), b.a);

        if (aberration > 0.0) {
            // norm: 0 at slider-min, 1 at 64 dp (192 px at 3× density, the slider max).
            float norm      = clamp(aberration / 192.0, 0.0, 1.0);
            // bandStart moves from tight rim (0.92) to nearly half the panel (0.52) as the
            // slider increases; spread and intensity both grow together.
            float bandStart = mix(0.92, 0.52, norm);
            float rimMask   = smoothstep(bandStart, 1.0, r);

            if (rimMask > 0.001) {
                float safeR = max(r, 1e-4);
                vec2  dir   = ndc / safeR;

                // Content refraction: R and B taps shifted in opposite radial directions.
                vec2  off = dir * (aberration * 0.25);
                half4 tR  = content.eval(clamp(coord + off, lo, hi));
                half4 tG  = content.eval(clamp(coord,       lo, hi));
                half4 tB  = content.eval(clamp(coord - off, lo, hi));
                b.rgb = mix(b.rgb, vec3(tR.r, tG.g, tB.b), rimMask * 0.5);

                // Additive angular rainbow: hue follows the pixel's angle around the panel
                // centre, so the full spectrum rings the rim continuously.
                float angle   = atan(dir.y, dir.x);
                float hue     = fract(angle * 0.15915 + 0.5 + hueShift);
                float H6      = hue * 6.0;
                vec3  rainbow = clamp(vec3(
                    abs(H6 - 3.0) - 1.0,
                    2.0 - abs(H6 - 2.0),
                    2.0 - abs(H6 - 4.0)
                ), 0.0, 1.0);
                // rimMask encodes spread; norm encodes intensity; both scale with slider.
                b = half4(min(b.rgb + rainbow * (rimMask * norm * 0.7), vec3(1.0)), tG.a);
            }
        }

        return b;
    }
"""

private const val GLASS_UNIFORM_PANEL_SIZE = "panelSize"
private const val GLASS_UNIFORM_BLUR_RADIUS = "blurRadius"
private const val GLASS_UNIFORM_SATURATION = "saturation"
private const val GLASS_UNIFORM_ABERRATION = "aberration"
private const val GLASS_UNIFORM_HUE_SHIFT = "hueShift"
private const val GLASS_SHADER_INPUT_NAME = "content"

/**
 * Shared state linking [Modifier.glassSource] (the content being sampled) with [Modifier.glass]
 * (the panel painting it as glass). Owns one [GraphicsLayer]: the source records into it on
 * every draw, the panel plays it back translated through a render effect that handles blur,
 * saturation, and (optionally) chromatic aberration.
 *
 * Obtain via [rememberGlassState]. One state per source/panel pair.
 */
@Stable
public class GlassState internal constructor(internal val layer: GraphicsLayer) {
    init {
        // Force offscreen compositing so renderEffect (set per-frame by panels) actually
        // applies during playback. Auto can elide the offscreen buffer and the blur silently
        // no-ops even on API 31+.
        layer.compositingStrategy = CompositingStrategy.Offscreen
    }

    internal var sourceCoords: LayoutCoordinates? = null
        private set

    private val panels: MutableSet<GlassNode> = mutableSetOf()

    internal fun setSourceCoords(coords: LayoutCoordinates?) {
        sourceCoords = coords
        invalidatePanels()
    }

    internal fun registerPanel(node: GlassNode) { panels.add(node) }
    internal fun unregisterPanel(node: GlassNode) { panels.remove(node) }
    internal fun invalidatePanels() { panels.forEach { it.invalidateDraw() } }
}

/** Remember a [GlassState] tied to the current composition's graphics-layer pool. */
@Composable
public fun rememberGlassState(): GlassState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassState(layer) }
}

/**
 * Marks the receiver as the source content for [Modifier.glass]. Records the receiver into the
 * [state]'s graphics layer on every draw and plays it back unblurred so the source still
 * appears normally on screen.
 *
 * **Ordering:** the source must draw _before_ the panel within the same parent. In a `Box`,
 * that's the natural declaration order (source first, panel second).
 *
 * @sample io.github.christophybarth.cookbook.samples.GlassSample
 */
public fun Modifier.glassSource(state: GlassState): Modifier =
    this then GlassSourceElement(state)

@Stable
private data class GlassSourceElement(
    val state: GlassState,
) : ModifierNodeElement<GlassSourceNode>() {
    override fun create(): GlassSourceNode = GlassSourceNode(state)
    override fun update(node: GlassSourceNode) { node.update(state) }
    override fun InspectorInfo.inspectableProperties() {
        name = "glassSource"
    }
}

internal class GlassSourceNode(
    private var state: GlassState,
) : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode {

    fun update(newState: GlassState) {
        if (newState !== state) {
            state.setSourceCoords(null)
            state = newState
            invalidateDraw()
        }
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        state.setSourceCoords(coordinates)
    }

    override fun ContentDrawScope.draw() {
        val w = size.width.toInt().coerceAtLeast(0)
        val h = size.height.toInt().coerceAtLeast(0)
        if (w == 0 || h == 0) {
            drawContent()
            return
        }
        // Source playback is unblurred. The paired glass node sets renderEffect to its glass
        // shader right before its own playback; clear it here so the source displays normally
        // each frame.
        state.layer.renderEffect = null
        state.layer.record(
            density = this,
            layoutDirection = layoutDirection,
            size = IntSize(w, h),
        ) {
            this@draw.drawContent()
        }
        drawLayer(state.layer)
        state.invalidatePanels()
    }

    override fun onDetach() {
        state.setSourceCoords(null)
    }
}

/**
 * Paints a glass panel on the receiver, sampling whatever is recorded by [Modifier.glassSource]
 * tied to the same [state]. **Defaults render as clear glass**: backdrop blur with a dark
 * legibility scrim, no saturation boost, no sheen, no specular border. Opt into frosted glass by
 * raising [saturation], [sheenAlpha], and [borderAlpha].
 *
 * Layers drawn back-to-front:
 *  1. Recorded backdrop, played back at the source's offset.
 *  2. **Blur** ([blurRadius]). API 31+ via [BlurEffect].
 *  3. **Saturation boost** ([saturation]). API 33+ via AGSL [RuntimeShader] chained ahead of
 *     the blur. `1f` = no change. Set to `1.4f`–`1.8f` for the "luminous glass" look; without
 *     it, Gaussian blur in linear RGB averages colours toward grey.
 *  4. **Rainbow rim** ([chromaticAberration]). API 33+ via AGSL [RuntimeShader]. When
 *     aberration is on, blur + saturation + the rim are baked into a single combined shader
 *     rather than chained (chaining a runtime shader after `BlurEffect` destroys the rim's
 *     raw colour edges). The rim is two-part: R and B taps shifted radially in opposite
 *     directions for refraction (G stays at the panel-local coord), plus an additive angular
 *     rainbow keyed off the pixel's angle around the panel centre. A dynamic `smoothstep`
 *     band (`bandStart = mix(0.92, 0.52, norm)`, so ~8% rim at small values, ~48% at the
 *     64 dp ceiling) confines both effects to the outer ring. `0.dp` off; `16.dp`–`32.dp`
 *     for a pronounced rainbow. Lower [tint] alpha if you don't want the fringe painted over.
 *  5. **Tint** ([tint]). Black-alpha for clear-glass legibility scrim; white-alpha for frost.
 *  6. **Diagonal sheen** ([sheenAlpha]). Bright top-left → transparent bottom-right. `0f` off.
 *  7. **Specular border** ([borderAlpha]). 1-px stroke along [shape] with a top-bright,
 *     side-fading gradient. `0f` off; turns a flat panel into a pane when on.
 *  8. The receiver's own content.
 *
 * On API 31–32 saturation and chromatic aberration are skipped (`RuntimeShader` requires 33+);
 * only the blur applies. On API 21–30 the blur silently no-ops; tint, sheen and border still
 * draw.
 *
 * Apply `Modifier.clip(shape)` _before_ this modifier to keep the blurred backdrop inside the
 * panel's outline; pass the same [shape] to this modifier so the specular border follows it.
 *
 * Two configurations to pattern-match the call sites:
 * ```kotlin
 * // Clear glass (default): blur + dark scrim only.
 * Modifier.clip(RoundedCornerShape(12.dp)).glass(state)
 *
 * // Frosted glass: full glassmorphism treatment with prism-edge refraction.
 * Modifier.clip(RoundedCornerShape(20.dp)).glass(
 *     state,
 *     blurRadius = 24.dp,
 *     saturation = 1.5f,
 *     chromaticAberration = 24.dp,
 *     tint = Color.White.copy(alpha = 0.18f),
 *     sheenAlpha = 0.22f,
 *     borderAlpha = 0.45f,
 *     shape = RoundedCornerShape(20.dp),
 * )
 * ```
 *
 * @param state Shared state from [rememberGlassState], also passed to [Modifier.glassSource].
 * @param blurRadius Blur radius applied to the layer playback.
 * @param saturation Multiplier applied to the blurred backdrop's chromaticity. `1f` = no change,
 *   `1.5f` = pleasantly luminous, `2f` = oversaturated. API 33+ only.
 * @param chromaticAberration Rainbow rim width/intensity. `0.dp` (default) to opt out; `16.dp`–`32.dp`
 *   for a clear prism; `64.dp` for a full-width rainbow. The `smoothstep` band grows dynamically with
 *   the value so spread and intensity scale together. API 33+ only.
 * @param hueShift Rotates the rainbow spectrum. `0f` = default colours; `0.5f` = 180° opposite;
 *   `1f` = full revolution back to start. No effect when `chromaticAberration == 0.dp`. API 33+ only.
 * @param tint Translucent overlay painted over the blurred + saturated backdrop. Default is a
 *   barely-there legibility scrim (`Black @ 8%`).
 * @param sheenAlpha Peak alpha of the diagonal white sheen gradient. Default `0.12f`; `0f` to disable.
 * @param borderAlpha Peak alpha of the specular border. Default `0.65f`; `0f` to disable.
 * @param shape Outline used for the specular border. Default is `RoundedCornerShape(16.dp)`.
 *
 * @sample io.github.christophybarth.cookbook.samples.GlassSample
 */
public fun Modifier.glass(
    state: GlassState,
    blurRadius: Dp = 12.dp,
    saturation: Float = 1.15f,
    chromaticAberration: Dp = 0.dp,
    hueShift: Float = 0f,
    tint: Color = Color.Black.copy(alpha = 0.08f),
    sheenAlpha: Float = 0.12f,
    borderAlpha: Float = 0.65f,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier {
    require(blurRadius.value >= 0f) { "glass: blurRadius must be >= 0, was $blurRadius" }
    require(saturation >= 0f) { "glass: saturation must be >= 0, was $saturation" }
    require(chromaticAberration.value >= 0f) {
        "glass: chromaticAberration must be >= 0, was $chromaticAberration"
    }
    return this then GlassElement(
        state, blurRadius, saturation, chromaticAberration, hueShift, tint, sheenAlpha, borderAlpha, shape,
    )
}

@Stable
private data class GlassElement(
    val state: GlassState,
    val blurRadius: Dp,
    val saturation: Float,
    val chromaticAberration: Dp,
    val hueShift: Float,
    val tint: Color,
    val sheenAlpha: Float,
    val borderAlpha: Float,
    val shape: Shape,
) : ModifierNodeElement<GlassNode>() {
    override fun create(): GlassNode = GlassNode(
        state, blurRadius, saturation, chromaticAberration, hueShift, tint, sheenAlpha, borderAlpha, shape,
    )

    override fun update(node: GlassNode) {
        node.update(
            state, blurRadius, saturation, chromaticAberration, hueShift, tint, sheenAlpha, borderAlpha, shape,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "glass"
        properties["blurRadius"] = blurRadius
        properties["saturation"] = saturation
        properties["chromaticAberration"] = chromaticAberration
        properties["hueShift"] = hueShift
        properties["tint"] = tint
        properties["sheenAlpha"] = sheenAlpha
        properties["borderAlpha"] = borderAlpha
        properties["shape"] = shape
    }
}

internal class GlassNode(
    private var state: GlassState,
    private var blurRadius: Dp,
    private var saturation: Float,
    private var chromaticAberration: Dp,
    private var hueShift: Float,
    private var tint: Color,
    private var sheenAlpha: Float,
    private var borderAlpha: Float,
    private var shape: Shape,
) : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode {

    private var ownCoords: LayoutCoordinates? = null

    /** Cached AGSL shaders. The `saturationShader` chains under `BlurEffect` for the default
     *  case (no chromatic aberration, full hardware blur). The `glassShader` is a single-pass
     *  combined effect (multi-tap blur + saturation + chromatic aberration) used when the
     *  caller opts into [chromaticAberration]; chaining a runtime shader after `BlurEffect`
     *  destroys the chromatic taps' input. Allocated lazily on first API-33+ draw and reused;
     *  uniforms are rewritten each frame. */
    private var saturationShader: RuntimeShader? = null
    private var glassShader: RuntimeShader? = null

    /** Per-panel offscreen layer used for the combined-shader path. Owned by this node, never
     *  touched by `glassSource`, so its `renderEffect` can be set without fighting over a
     *  shared layer's state. Acquired lazily; released in [onDetach]. */
    private var panelLayer: GraphicsLayer? = null

    fun update(
        newState: GlassState,
        newBlurRadius: Dp,
        newSaturation: Float,
        newChromaticAberration: Dp,
        newHueShift: Float,
        newTint: Color,
        newSheenAlpha: Float,
        newBorderAlpha: Float,
        newShape: Shape,
    ) {
        if (newState !== state) {
            state.unregisterPanel(this)
            state = newState
            if (isAttached) state.registerPanel(this)
        }
        blurRadius = newBlurRadius
        saturation = newSaturation
        chromaticAberration = newChromaticAberration
        hueShift = newHueShift
        tint = newTint
        sheenAlpha = newSheenAlpha
        borderAlpha = newBorderAlpha
        shape = newShape
        invalidateDraw()
    }

    override fun onAttach() {
        state.registerPanel(this)
    }

    override fun onDetach() {
        state.unregisterPanel(this)
        ownCoords = null
        saturationShader = null
        glassShader = null
        panelLayer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        panelLayer = null
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        ownCoords = coordinates
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        val src = state.sourceCoords
        val self = ownCoords
        if (src != null && self != null) paintBackdrop(src, self)
        if (tint.alpha > 0f) drawRect(color = tint)
        if (sheenAlpha > 0f) drawSheen()
        if (borderAlpha > 0f) drawSpecularBorder()
        drawContent()
    }

    /**
     * Backdrop. Two render paths:
     *
     *  - **Default (`chromaticAberration == 0`)**: set `state.layer.renderEffect` to the
     *    chained blur+saturation and play it. Hardware-blur quality. This pattern shares a
     *    layer between source and panel and works because chained runtime-shader effects
     *    apply correctly even when the source's modifier and the panel's modifier both call
     *    `drawLayer(state.layer)` in the same frame.
     *  - **With aberration (`chromaticAberration > 0`)**: copy the source's raw playback into
     *    [panelLayer] (a private offscreen layer owned by this node), set its `renderEffect`
     *    to the combined glass shader, and play that. We can't apply a standalone runtime
     *    shader to `state.layer` directly; the source's `drawLayer(state.layer)` and the
     *    panel's `drawLayer(state.layer)` end up using the same RenderNode at flush, and
     *    setting `state.layer.renderEffect` from the panel doesn't apply for standalone
     *    runtime-shader effects (only chained ones); a private layer side-steps that.
     */
    private fun ContentDrawScope.paintBackdrop(src: LayoutCoordinates, self: LayoutCoordinates) {
        if (!src.isAttached || !self.isAttached) return
        val origin = self.localPositionOf(src, Offset.Zero)
        // origin = source's (0,0) in panel-local space. -origin = panel's (0,0) in source.
        val panelOriginInSource = Offset(-origin.x, -origin.y)
        val blurPx = blurRadius.toPx()
        val aberrationPx = chromaticAberration.toPx()
        val wantAberration =
            aberrationPx > 0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        if (wantAberration) {
            paintAberrationBackdrop(blurPx, aberrationPx, panelOriginInSource)
            return
        }

        state.layer.renderEffect = composeRenderEffect(blurPx, 0f, size, panelOriginInSource)
        clipRect {
            translate(origin.x, origin.y) {
                drawLayer(state.layer)
            }
        }
    }

    /**
     * Render the combined glass shader on a private per-panel offscreen layer. The recording
     * step copies `state.layer`'s playback (with effect=null, since the source set it that way
     * and we never mutate it from the panel) into [panelLayer], translated so panel-local
     * (0,0) lines up with the layer's (0,0). Then [panelLayer].renderEffect is set to the
     * combined shader and played at the panel's position.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun ContentDrawScope.paintAberrationBackdrop(
        blurPx: Float,
        aberrationPx: Float,
        panelOriginInSource: Offset,
    ) {
        val w = size.width.toInt().coerceAtLeast(0)
        val h = size.height.toInt().coerceAtLeast(0)
        if (w == 0 || h == 0) return
        val layer = panelLayer
            ?: requireGraphicsContext().createGraphicsLayer().also {
                it.compositingStrategy = CompositingStrategy.Offscreen
                panelLayer = it
            }
        layer.renderEffect = null
        layer.record(
            density = this,
            layoutDirection = layoutDirection,
            size = IntSize(w, h),
        ) {
            // Pull the panel's slice of the source content into panelLayer. State.layer's
            // renderEffect is null at this moment (set by the source modifier earlier this
            // frame and never touched by panels), so the playback is the raw source.
            translate(-panelOriginInSource.x, -panelOriginInSource.y) {
                drawLayer(state.layer)
            }
        }
        layer.renderEffect = buildCombinedGlassEffect(blurPx, aberrationPx, hueShift, size, Offset.Zero)
        clipRect {
            drawLayer(layer)
        }
    }

    /**
     * Build the layer's `renderEffect`. Picks between the hardware-blur chain (default, when
     * aberration is off) and the single combined AGSL shader (when aberration is on).
     */
    private fun ContentDrawScope.composeRenderEffect(
        blurPx: Float,
        aberrationPx: Float,
        panelSize: Size,
        panelOriginInSource: Offset,
    ): RenderEffect? {
        val wantBlur = blurPx > 0f
        val wantSaturation = saturation != 1f
        val wantAberration = aberrationPx > 0f
        if (!wantBlur && !wantSaturation && !wantAberration) return null
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && wantAberration ->
                buildCombinedGlassEffect(blurPx, aberrationPx, hueShift, panelSize, panelOriginInSource)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && wantSaturation ->
                buildBlurSaturationChain(blurPx)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && wantBlur ->
                BlurEffect(blurPx, blurPx, TileMode.Clamp)
            else -> null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildBlurSaturationChain(blurPx: Float): RenderEffect {
        var effect: AndroidRenderEffect? = if (blurPx > 0f) {
            AndroidRenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
        } else null
        if (saturation != 1f) {
            val shader = saturationShader
                ?: RuntimeShader(SATURATION_SHADER_SOURCE).also { saturationShader = it }
            shader.setFloatUniform(SATURATION_UNIFORM_AMOUNT, saturation)
            val saturate = AndroidRenderEffect.createRuntimeShaderEffect(
                shader, SATURATION_SHADER_INPUT_NAME,
            )
            effect = effect?.let { AndroidRenderEffect.createChainEffect(saturate, it) } ?: saturate
        }
        return checkNotNull(effect).asComposeRenderEffect()
    }

    /**
     * Combined glass shader: multi-tap pseudo-Gaussian blur + Rec.709 saturation + radial
     * chromatic aberration on raw input, in a single render effect. No chain: the aberration
     * taps need raw colour edges, which any prior blur destroys.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildCombinedGlassEffect(
        blurPx: Float,
        aberrationPx: Float,
        hueShift: Float,
        panelSize: Size,
        @Suppress("UNUSED_PARAMETER") panelOriginInSource: Offset,
    ): RenderEffect {
        val shader = glassShader
            ?: RuntimeShader(GLASS_SHADER_SOURCE).also { glassShader = it }
        shader.setFloatUniform(GLASS_UNIFORM_PANEL_SIZE, panelSize.width, panelSize.height)
        shader.setFloatUniform(GLASS_UNIFORM_BLUR_RADIUS, blurPx)
        shader.setFloatUniform(GLASS_UNIFORM_SATURATION, saturation)
        shader.setFloatUniform(GLASS_UNIFORM_ABERRATION, aberrationPx)
        shader.setFloatUniform(GLASS_UNIFORM_HUE_SHIFT, hueShift)
        return AndroidRenderEffect
            .createRuntimeShaderEffect(shader, GLASS_SHADER_INPUT_NAME)
            .asComposeRenderEffect()
    }

    /**
     * Diagonal sheen: bright at top-left, fading to transparent at bottom-right. Drawn as a
     * full-bounds rect; relies on the caller's outer `Modifier.clip(shape)` to keep it inside
     * the panel outline.
     */
    private fun ContentDrawScope.drawSheen() {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = sheenAlpha),
                    Color.White.copy(alpha = sheenAlpha * 0.18f),
                    Color.Transparent,
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
        )
    }

    /**
     * 1-px specular border that follows [shape]. Top-bright fading down the diagonal; reads as
     * a light source from above-left.
     */
    private fun ContentDrawScope.drawSpecularBorder() {
        val strokePx = 1.dp.toPx()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.20f),
                Color.White.copy(alpha = borderAlpha * 0.55f),
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        )
        when (val outline = shape.createOutline(size, layoutDirection, this)) {
            is Outline.Rectangle -> drawRect(
                brush = brush,
                topLeft = Offset(outline.rect.left + strokePx / 2, outline.rect.top + strokePx / 2),
                size = Size(outline.rect.width - strokePx, outline.rect.height - strokePx),
                style = Stroke(width = strokePx),
            )
            is Outline.Rounded -> {
                val rr = outline.roundRect
                val tl = rr.topLeftCornerRadius
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(rr.left + strokePx / 2, rr.top + strokePx / 2),
                    size = Size(rr.width - strokePx, rr.height - strokePx),
                    cornerRadius = CornerRadius(
                        (tl.x - strokePx / 2).coerceAtLeast(0f),
                        (tl.y - strokePx / 2).coerceAtLeast(0f),
                    ),
                    style = Stroke(width = strokePx),
                )
            }
            is Outline.Generic -> drawPath(
                path = outline.path,
                brush = brush,
                style = Stroke(width = strokePx),
            )
        }
    }
}
