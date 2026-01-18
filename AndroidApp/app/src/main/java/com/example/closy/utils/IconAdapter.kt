package com.example.closy.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.closy.R

class IconAdapter(
    context: Context,
    private val items: List<String>,
    private val icons: List<Any>
) : ArrayAdapter<String>(context, R.layout.list_item_with_icon, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_with_icon, parent, false)

        val textView = view.findViewById<TextView>(R.id.itemText)
        val iconView = view.findViewById<ImageView>(R.id.itemIcon)

        textView.text = items[position]

        val icon = icons[position]
        when (icon) {
            is String -> {
                if (icon.startsWith("#")) {
                    val drawable = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor(icon))
                        setStroke(2, Color.parseColor("#DDDDDD"))
                    }
                    iconView.setImageDrawable(drawable)
                }
            }
            is Int -> {
                iconView.setImageResource(icon)
                iconView.setColorFilter(null) // Reset barvnih filtrov
            }
        }
        return view
    }
}