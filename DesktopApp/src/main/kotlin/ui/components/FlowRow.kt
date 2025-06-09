package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Int>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        val horizontalSpacing = horizontalArrangement.spacing.roundToPx()
        val verticalSpacing = verticalArrangement.spacing.roundToPx()

        val currentSequence = mutableListOf<Int>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        val placeables = measurables.mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints.copy(
                minWidth = 0,
                minHeight = 0
            ))

            if (currentMainAxisSize + placeable.width + (if (currentSequence.isEmpty()) 0 else horizontalSpacing) > constraints.maxWidth && currentSequence.isNotEmpty()) {
                // Add current sequence to list
                sequences.add(currentSequence.toList())
                crossAxisSizes.add(currentCrossAxisSize)
                crossAxisPositions.add(if (crossAxisPositions.isEmpty()) 0 else crossAxisPositions.last() + crossAxisSizes.last() + verticalSpacing)

                // Reset for next row
                currentSequence.clear()
                currentMainAxisSize = 0
                currentCrossAxisSize = 0
            }

            currentSequence.add(index)
            currentMainAxisSize += placeable.width + if (currentSequence.size > 1) horizontalSpacing else 0
            currentCrossAxisSize = maxOf(currentCrossAxisSize, placeable.height)

            placeable
        }

        // Add the last sequence if not empty
        if (currentSequence.isNotEmpty()) {
            sequences.add(currentSequence.toList())
            crossAxisSizes.add(currentCrossAxisSize)
            crossAxisPositions.add(if (crossAxisPositions.isEmpty()) 0 else crossAxisPositions.last() + crossAxisSizes.last() + verticalSpacing)
        }

        // Calculate the total width - avoid using sumOf
        val maxWidth = sequences.maxOfOrNull { seq ->
            if (seq.isEmpty()) 0 else {
                var width = 0
                for (i in seq) {
                    width += placeables[i].width
                }
                width + (seq.size - 1) * horizontalSpacing
            }
        } ?: 0

        // Calculate total height
        val totalHeight = if (crossAxisPositions.isEmpty() || crossAxisSizes.isEmpty()) 0 
                         else crossAxisPositions.last() + crossAxisSizes.last()

        layout(
            width = maxWidth.coerceAtMost(constraints.maxWidth),
            height = totalHeight.coerceAtMost(constraints.maxHeight)
        ) {
            sequences.forEachIndexed { i, seq ->
                var currentX = 0

                seq.forEach { index ->
                    val placeable = placeables[index]

                    placeable.place(
                        x = currentX,
                        y = crossAxisPositions[i]
                    )

                    currentX += placeable.width + horizontalSpacing
                }
            }
        }
    }
}