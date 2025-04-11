package com.example.teams_app

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import java.util.HashMap

class MemberAnimationManager(private val context: Context) {

    // Map to store predefined animations for team members
    private val memberAnimations = HashMap<String, MemberAnimation>()

    init {
        // Initialize with some predefined animations
        setupDefaultAnimations()
    }

    private fun setupDefaultAnimations() {
        // Define different animation types for different talents/roles
        memberAnimations["Designer"] = MemberAnimation(
            profileAnimation = { view ->
                // Designers get a creative scaling animation
                val scale = ScaleAnimation(
                    1f, 1.2f, 1f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                scale.duration = 800
                scale.interpolator = OvershootInterpolator()
                scale.repeatMode = Animation.REVERSE
                scale.repeatCount = 1
                view.startAnimation(scale)
            },
            mindsetAnimation = R.anim.color_pulse_animation,
            favoriteItemAnimation = { view ->
                val translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, -0.2f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f
                )
                translate.duration = 600
                translate.interpolator = OvershootInterpolator()
                view.startAnimation(translate)
            }
        )

        memberAnimations["Developer"] = MemberAnimation(
            profileAnimation = { view ->
                // Developers get a rotation animation symbolizing code cycle
                val rotate = RotateAnimation(
                    0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                rotate.duration = 1200
                rotate.interpolator = LinearInterpolator()
                view.startAnimation(rotate)
            },
            mindsetAnimation = R.anim.typing_animation,
            favoriteItemAnimation = { view ->
                val bounceIn = AnimationUtils.loadAnimation(context, R.anim.bounce_in)
                view.startAnimation(bounceIn)
            }
        )

        memberAnimations["Manager"] = MemberAnimation(
            profileAnimation = { view ->
                // Managers get a subtle professional animation
                val pulse = ScaleAnimation(
                    1f, 1.05f, 1f, 1.05f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                pulse.duration = 500
                pulse.repeatMode = Animation.REVERSE
                pulse.repeatCount = 1
                view.startAnimation(pulse)
            },
            mindsetAnimation = R.anim.slide_up_animation,
            favoriteItemAnimation = { view ->
                val slideRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
                view.startAnimation(slideRight)
            }
        )

        memberAnimations["Marketing"] = MemberAnimation(
            profileAnimation = { view ->
                // Marketing gets attention-grabbing animation
                val bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_animation)
                bounce.interpolator = BounceInterpolator()
                view.startAnimation(bounce)
            },
            mindsetAnimation = R.anim.fade_pulse_animation,
            favoriteItemAnimation = { view ->
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                view.startAnimation(fadeIn)
            }
        )

        // QA team member animations
        memberAnimations["QA"] = MemberAnimation(
            profileAnimation = { view ->
                // QA gets a methodical checking animation
                val checkAnim = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, -0.05f,
                    Animation.RELATIVE_TO_SELF, 0.05f
                )
                checkAnim.duration = 300
                checkAnim.repeatCount = 2
                checkAnim.repeatMode = Animation.REVERSE
                view.startAnimation(checkAnim)
            },
            mindsetAnimation = R.anim.verify_animation,
            favoriteItemAnimation = { view ->
                val slideFromTop = AnimationUtils.loadAnimation(context, R.anim.slide_in_top)
                view.startAnimation(slideFromTop)
            }
        )

        // Artistic team member animations
        memberAnimations["Artist"] = MemberAnimation(
            profileAnimation = { view ->
                // Artists get a creative flourish animation
                val rotate = RotateAnimation(
                    -5f, 5f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 1.0f
                )
                rotate.duration = 1000
                rotate.repeatMode = Animation.REVERSE
                rotate.repeatCount = 1
                view.startAnimation(rotate)
            },
            mindsetAnimation = R.anim.color_transition_animation,
            favoriteItemAnimation = { view ->
                val fadeWithScale = AnimationUtils.loadAnimation(context, R.anim.fade_scale_in)
                view.startAnimation(fadeWithScale)
            }
        )

        // Default animation for any other role
        memberAnimations["default"] = MemberAnimation(
            profileAnimation = { view ->
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                view.startAnimation(fadeIn)
            },
            mindsetAnimation = R.anim.fade_in,
            favoriteItemAnimation = { view ->
                val slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
                view.startAnimation(slideIn)
            }
        )
    }

    // Method to get animation for a specific role
    fun getAnimationForRole(role: String): MemberAnimation {
        // Try to get specific animation for this role, fall back to default
        return memberAnimations[role] ?: memberAnimations["default"]!!
    }

    // Method to add custom animation for a team member
    fun addCustomAnimation(role: String, animation: MemberAnimation) {
        memberAnimations[role] = animation
    }

    // Data class to hold different animations for a member
    data class MemberAnimation(
        val profileAnimation: (View) -> Unit,
        val mindsetAnimation: Int,
        val favoriteItemAnimation: (View) -> Unit
    )
}