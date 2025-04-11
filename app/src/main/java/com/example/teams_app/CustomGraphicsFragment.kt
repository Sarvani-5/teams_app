package com.example.teams_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class CustomGraphicsFragment : Fragment() {

    private lateinit var drawingView: InteractiveDrawingView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Simply create and return the interactive drawing view
        drawingView = InteractiveDrawingView(requireContext())
        return drawingView
    }
}