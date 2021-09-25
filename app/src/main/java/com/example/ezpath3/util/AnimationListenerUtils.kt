package com.example.ezpath3.util

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun ViewPropertyAnimator.makeListener(onStart : (animation : Animator?) -> Unit = {},
                                              onEnd : (animation : Animator?) -> Unit = {},
                                              onCancel : (animation : Animator?) -> Unit = {},
                                              onRepeat : (animation : Animator?) -> Unit = {}) : ViewPropertyAnimator {
    return this.setListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            onStart(animation)
        }

        override fun onAnimationEnd(animation: Animator?) {
            onEnd(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
            onCancel(animation)
        }

        override fun onAnimationRepeat(animation: Animator?) {
            onRepeat(animation)
        }

    })
}

 fun Transition.makeListener(onStart : (transition : Transition) -> Unit = {},
                                    onEnd : (transition : Transition) -> Unit = {},
                                    onCancel : (transition : Transition) -> Unit = {},
                                    onPause : (transition : Transition) -> Unit = {},
                                    onResume : (transition : Transition) -> Unit = {}) : Transition {
    this.addListener(object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) {
            onStart(transition)
        }

        override fun onTransitionEnd(transition: Transition) {
            onEnd(transition)
        }

        override fun onTransitionCancel(transition: Transition) {
            onCancel(transition)
        }

        override fun onTransitionPause(transition: Transition) {
            onPause(transition)
        }

        override fun onTransitionResume(transition: Transition) {
            onResume(transition)
        }

    })
    return this

}

 fun DrawerLayout.makeListener(onSlide : (drawerView: View, slideOffset: Float) -> Unit = { drawerView, slideoOffSet ->},
                                      onOpened : (drawerView: View) -> Unit = {},
                                      onClosed : (drawerView: View) -> Unit = {},
                                      onStateChanged : (newState : Int) -> Unit = {}) {
    this.addDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            onSlide(drawerView, slideOffset)
        }

        override fun onDrawerOpened(drawerView: View) {
            onOpened(drawerView)
        }

        override fun onDrawerClosed(drawerView: View) {
            onClosed(drawerView)
        }

        override fun onDrawerStateChanged(newState: Int) {
            onStateChanged(newState)
        }

    })
}

 fun ConstraintLayout.hide() {
    this.animate().apply {
        duration = 500L
        alpha(0.0f)
        makeListener(onEnd = {this@hide.visibility = View.INVISIBLE})
    }
}

 fun ConstraintLayout.show() {
    this.animate().apply {
        duration = 500L
        alpha(1.0f)
        makeListener(onEnd = {this@show.visibility = View.VISIBLE})
    }
}

 fun BottomSheetBehavior<ConstraintLayout>.makeBottomSheetCallback(onStateChanged : (bottomSheet : View, newState : Int) -> Unit = {bottomSheet, newState ->},
                                                                   onSlide : (bottomShhet : View, slideOffset : Float) -> Unit = {bottomSheet, slideOffset ->}) {
     this.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
         override fun onStateChanged(bottomSheet: View, newState: Int) {
             onStateChanged(bottomSheet, newState)
         }

         override fun onSlide(bottomSheet: View, slideOffset: Float) {
             onSlide(bottomSheet, slideOffset)
         }

     })
 }

 fun makeItemTouchCallback(dragDirs : Int = 0, swipeDirs : Int = 0, longPressEnabled : Boolean = false, onMove : (recyclerView : RecyclerView, viewHolder : RecyclerView.ViewHolder, target : RecyclerView.ViewHolder) -> Boolean = {recyclerView, viewHolder, target -> false},
                           isLongPressEnabled : (enabled : Boolean) -> Boolean = {false},
                           getMovementFlags : (recyclerView : RecyclerView, viewHolder : RecyclerView.ViewHolder, dragDirs : Int, swipeDirs : Int) -> Int = {recyclerView, viewHolder, dragDirs, swipeDirs -> 0},
                           doNotAllowSwipeIf : (recyclerView : RecyclerView, viewHolder : RecyclerView.ViewHolder) -> Boolean = {recyclerView, viewHolder -> false},
                           onSwiped : (viewHolder : RecyclerView.ViewHolder, direction : Int) -> Unit = {viewHolder , direction -> },
                           clearView : (recyclerView : RecyclerView, viewHolder : RecyclerView.ViewHolder) -> Unit = {recyclerView, viewHolder ->}) : ItemTouchHelper.SimpleCallback {

     return object : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
         override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
             return onMove(recyclerView, viewHolder, target)
         }

         override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
             onSwiped(viewHolder, direction)
         }

         override fun isLongPressDragEnabled(): Boolean {
             return isLongPressEnabled(longPressEnabled)
         }

         override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
             return getMovementFlags(recyclerView, viewHolder, dragDirs, swipeDirs)
         }

         override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
             if (doNotAllowSwipeIf(recyclerView, viewHolder)) return 0
             return super.getSwipeDirs(recyclerView, viewHolder)
         }

         override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
             super.clearView(recyclerView, viewHolder)
             clearView(recyclerView, viewHolder)
         }

     }

 }