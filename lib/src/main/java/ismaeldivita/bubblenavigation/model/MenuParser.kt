package ismaeldivita.bubblenavigation.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Xml
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import ismaeldivita.bubblenavigation.R
import ismaeldivita.bubblenavigation.util.getValueFromAttr
import org.xmlpull.v1.XmlPullParser.*

internal class MenuParser(private val context: Context) {

    private companion object {
        const val XML_MENU_TAG = "menu"
        const val XML_MENU_ITEM_TAG = "item"
    }

    fun parse(@MenuRes menuRes: Int): Menu {
        @SuppressLint("ResourceType")
        val parser = context.resources.getLayout(menuRes)
        val attrs = Xml.asAttributeSet(parser)

        skipMenuTagStart(parser)

        return parseMenu(parser, attrs)
    }

    private fun skipMenuTagStart(parser: XmlResourceParser) {
        var currentEvent = parser.eventType
        do {
            if (currentEvent == START_TAG) {
                val name = parser.name
                require(name == XML_MENU_TAG) { "Expecting menu, got $name" }
                break
            }
            currentEvent = parser.next()
        } while (currentEvent != END_DOCUMENT)
    }

    private fun parseMenu(parser: XmlResourceParser, attrs: AttributeSet): Menu {
        val items = mutableListOf<MenuItem>()
        var eventType = parser.eventType
        var isEndOfMenu = false

        var badgeColor = 0
        var disabledColor = 0
        var unselectedColor = 0

        while (!isEndOfMenu) {
            val name = parser.name
            when {
                eventType == START_TAG && name == XML_MENU_TAG -> {
                    val sAttr = context.obtainStyledAttributes(attrs, R.styleable.BubbleMenu)
                    badgeColor = readBadgeColor(sAttr)
                    disabledColor = readDisabledColor(sAttr)
                    unselectedColor = readUnselectedColor(sAttr)
                    sAttr.recycle()
                }
                eventType == START_TAG && name == XML_MENU_ITEM_TAG -> {
                    val item = parseMenuItem(attrs)
                    items.add(
                        item.copy(
                            badgeColor = badgeColor,
                            disabledColor = disabledColor,
                            unselectedColor = unselectedColor
                        )
                    )
                }
                eventType == END_TAG && name == XML_MENU_TAG -> isEndOfMenu = true
                eventType == END_DOCUMENT -> throw RuntimeException("Unexpected end of document")

            }
            eventType = parser.next()
        }
        return Menu(
            items = items,
            badgeColor = badgeColor,
            disabledColor = disabledColor,
            unselectedColor = unselectedColor
        )
    }

    private fun parseMenuItem(attrs: AttributeSet): MenuItem {
        val sAttr = context.obtainStyledAttributes(attrs, R.styleable.BubbleMenuItem)

        val item = MenuItem(
            id = sAttr.getResourceId(R.styleable.BubbleMenuItem_android_id, 0),
            title = sAttr.getText(R.styleable.BubbleMenuItem_android_title),
            icon = sAttr.getResourceId(R.styleable.BubbleMenuItem_android_icon, 0),
            enabled = sAttr.getBoolean(R.styleable.BubbleMenuItem_android_enabled, true),
            iconColor = readIconActiveColor(sAttr),
            tintMode = readIconTintMode(sAttr),
            textColor = readTextActiveColor(sAttr),
            backgroundColor = readBackgroundColor(sAttr)
        )

        sAttr.recycle()
        return item
    }

    private fun readIconTintMode(sAttr: TypedArray): PorterDuff.Mode? =
        when (sAttr.getInt(R.styleable.BubbleMenuItem_bnv_iconTintMode, -1)) {
            3 -> PorterDuff.Mode.SRC_OVER
            5 -> PorterDuff.Mode.SRC_IN
            9 -> PorterDuff.Mode.SRC_ATOP
            14 -> PorterDuff.Mode.MULTIPLY
            15 -> PorterDuff.Mode.SCREEN
            16 -> PorterDuff.Mode.ADD
            else -> null
        }

    private fun readIconActiveColor(sAttr: TypedArray): Int = sAttr.getColor(
        R.styleable.BubbleMenuItem_bnv_iconColor,
        context.getValueFromAttr(R.attr.colorAccent)
    )

    private fun readBadgeColor(sAttr: TypedArray): Int = sAttr.getColor(
        R.styleable.BubbleMenu_bnv_badgeColor,
        ContextCompat.getColor(context, R.color.bnv_default_badge_tint_color)
    )

    private fun readDisabledColor(sAttr: TypedArray): Int = sAttr.getColor(
        R.styleable.BubbleMenu_bnv_disabledColor,
        context.getValueFromAttr(R.attr.colorButtonNormal)
    )

    private fun readUnselectedColor(sAttr: TypedArray): Int = sAttr.getColor(
        R.styleable.BubbleMenu_bnv_unselectedColor,
        ContextCompat.getColor(context, R.color.bnv_default_unselected_color)
    )

    private fun readTextActiveColor(sAttr: TypedArray): Int =
        sAttr.getColor(
            R.styleable.BubbleMenuItem_bnv_textActiveColor,
            readIconActiveColor(sAttr)
        )

    private fun readBackgroundColor(sAttr: TypedArray): Int {
        val iconTintColor = readIconActiveColor(sAttr)
        val defaultColor = Color.argb(
            (Color.alpha(iconTintColor) * 0.15).toInt(),
            Color.red(iconTintColor),
            Color.green(iconTintColor),
            Color.blue(iconTintColor)
        )
        return sAttr.getColor(
            R.styleable.BubbleMenuItem_bnv_backgroundColor,
            defaultColor
        )
    }

}