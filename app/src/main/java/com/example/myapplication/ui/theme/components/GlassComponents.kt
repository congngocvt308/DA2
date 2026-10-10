package com.cong.smartalarm.ui.theme.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ---------------------------------------------------------------------------
// Các component soft UI "thay thế tại chỗ": cùng tham số với bản Material3 tương ứng
// (Card, Button, AlertDialog, FloatingActionButton) để đổi tên là chạy.
// containerColor được phủ lên bề mặt trắng nổi (neumorphism) đúng như màu đã truyền.
// ---------------------------------------------------------------------------

/** Màu nền Material -> lớp phủ trên bề mặt trắng. Trong suốt = giữ bề mặt trắng. */
private fun glassTint(container: Color): Brush =
    if (container.alpha == 0f) SolidColor(Color.Transparent) else SolidColor(container)

/** Card nổi mềm — thay cho androidx.compose.material3.Card. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCardImpl(null, modifier, true, shape, colors, border, null, content)
}

/** Card nổi mềm bấm được — thay cho Card(onClick = ...). */
@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCardImpl(onClick, modifier, enabled, shape, colors, border, interactionSource, content)
}

@Composable
private fun GlassCardImpl(
    onClick: (() -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    shape: Shape,
    colors: CardColors,
    border: BorderStroke?,
    interactionSource: MutableInteractionSource?,
    content: @Composable ColumnScope.() -> Unit
) {
    val container = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    // Màu mặc định của Card (surfaceContainerHighest) -> giữ thẻ trắng nổi trên nền xám nhạt
    val isDefault = container == MaterialTheme.colorScheme.surfaceContainerHighest
    LiquidGlass(
        modifier = modifier.then(if (border != null) Modifier.border(border, shape) else Modifier),
        shape = shape,
        tint = if (isDefault) SolidColor(Color.Transparent) else glassTint(container),
        elevation = 10.dp,
        contentAlignment = Alignment.TopStart,
        propagateMinConstraints = true,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(content = content)
        }
    }
}

/** Nút nổi mềm tô màu — thay cho androidx.compose.material3.Button. */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val container = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    GlassPressable(
        onClick = onClick,
        modifier = modifier.then(if (border != null) Modifier.border(border, shape) else Modifier),
        enabled = enabled,
        shape = shape,
        tint = glassTint(container),
        elevation = if (elevation == null) 0.dp else 8.dp,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides LocalTextStyle.current.merge(MaterialTheme.typography.labelLarge)
        ) {
            Row(
                Modifier
                    .defaultMinSize(ButtonDefaults.MinWidth, ButtonDefaults.MinHeight)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

/** FAB nổi mềm — thay cho androidx.compose.material3.FloatingActionButton. */
@Composable
fun GlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    GlassPressable(
        onClick = onClick,
        modifier = modifier.defaultMinSize(56.dp, 56.dp),
        shape = shape,
        tint = glassTint(containerColor),
        elevation = 14.dp,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { content() }
        }
    }
}

/** Bề mặt bấm được: lún nhẹ khi nhấn (soft press 150 ms), không nảy. */
@Composable
private fun GlassPressable(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    shape: Shape,
    tint: Brush,
    elevation: Dp,
    interactionSource: MutableInteractionSource?,
    content: @Composable () -> Unit
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(SoftPressMillis),
        label = "softPress"
    )
    LiquidGlass(
        modifier = modifier.graphicsLayer {
            scaleX = 1f - 0.02f * press
            scaleY = 1f - 0.02f * press
            alpha = if (enabled) 1f else 0.5f
        },
        shape = shape,
        tint = tint,
        elevation = elevation,
        propagateMinConstraints = true,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interaction,
        indication = null
    ) {
        Box(propagateMinConstraints = true) { content() }
    }
}

/** Nền panel cho dialog: trắng, chữ gần đen luôn dễ đọc. */
val GlassDialogTint: Brush = SolidColor(Color.Transparent)

/** Panel nổi mềm cho Dialog tuỳ biến. */
@Composable
fun GlassDialogSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    LiquidGlass(
        modifier = modifier,
        shape = shape,
        tint = GlassDialogTint,
        elevation = 24.dp,
        contentAlignment = Alignment.TopStart,
        propagateMinConstraints = true
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Column(content = content)
        }
    }
}

/** AlertDialog nổi mềm — thay cho androidx.compose.material3.AlertDialog. */
@Composable
fun GlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        GlassDialogSurface(
            modifier = modifier.widthIn(min = 280.dp, max = 560.dp),
            shape = shape
        ) {
            Column(Modifier.padding(24.dp)) {
                if (icon != null) {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) { icon() }
                    }
                }
                if (title != null) {
                    CompositionLocalProvider(
                        LocalContentColor provides titleContentColor,
                        LocalTextStyle provides MaterialTheme.typography.headlineSmall
                    ) {
                        Box(
                            Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            contentAlignment = if (icon != null) Alignment.Center else Alignment.CenterStart
                        ) { title() }
                    }
                }
                if (text != null) {
                    CompositionLocalProvider(
                        LocalContentColor provides textContentColor,
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium
                    ) {
                        Box(Modifier.weight(1f, fill = false).padding(bottom = 24.dp)) { text() }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                        dismissButton?.invoke()
                        if (dismissButton != null) Spacer(Modifier.size(8.dp))
                        confirmButton()
                    }
                }
            }
        }
    }
}
