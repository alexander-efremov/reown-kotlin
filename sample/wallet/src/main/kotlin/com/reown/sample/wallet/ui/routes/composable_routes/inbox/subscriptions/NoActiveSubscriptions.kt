package com.reown.sample.wallet.ui.routes.composable_routes.inbox.subscriptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reown.sample.wallet.R
import kotlinx.coroutines.delay

@Composable
fun NoActiveSubscriptions(onDiscoverMoreClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val isVisible = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(300)
            isVisible.value = true
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = isVisible.value,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(1200), initialOffsetY = { 60 })
        ) {
            val colors = listOf(
                Color.Transparent,
                Color(0xff6eedd6), Color(0xff6eedd6),
                Color(0xff58c0e7), Color(0xff58c0e7), Color(0xff58c0e7),
                Color.Transparent
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Canvas(
                    modifier = Modifier
                        .requiredSize(400.dp)
                        .rotate(250f)
                        .blur(60.dp)
                        .alpha(0.5f),
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(colors = colors),
                        center = center,
                        radius = size.minDimension / 3f
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Image(painter = painterResource(id = R.drawable.ic_discover), contentDescription = null)
                Text(
                    text = "Add your first app",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(600),
                        textAlign = TextAlign.Center,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.width(240.dp),
                    text = "Head over to “Discover” and subscribe to one of our apps to start receiving notifications",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                    onClick = onDiscoverMoreClicked
                ) {
                    Text(
                        text = "Discover apps",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight(600),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.onBackground
                        )
                    )
                }
            }
        }
    }
}