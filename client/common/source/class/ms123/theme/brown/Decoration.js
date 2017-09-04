/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
qx.Theme.define("ms123.theme.brown.Decoration", {
	extend: qx.theme.simple.Decoration,

	decorations: {
	  "hover-css" : {
      decorator : [
        qx.ui.decoration.MLinearBackgroundGradient
      ],

      style : {
        startColorPosition : 0,
        endColorPosition : 100,
        startColor : "#CCCCCC",
        endColor : "#CCCCCC"
      }
    },

	  "pressed-css" : {
      decorator : [
        qx.ui.decoration.MLinearBackgroundGradient
      ],

      style : {
        startColorPosition : 0,
        endColorPosition : 100,
        startColor : "#f87925",
        endColor : "#f87925"
      }
    },

    "button-box" :
    {
      decorator : [
        qx.ui.decoration.MLinearBackgroundGradient,
        qx.ui.decoration.MBorderRadius,
        qx.ui.decoration.MSingleBorder,
        qx.ui.decoration.MBackgroundColor
      ],

      style :
      {
        radius : 1,
        width : 1,
        color : "button-border",
        gradientStart : ["button-box-bright", 40],
        gradientEnd : ["button-box-bright", 70],
        backgroundColor : "button-box-bright"
      }
    },
    "button-box-pressed" :
    {
      include : "button-box",

      style :
      {
        gradientStart : ["button-border", 40],
        gradientEnd : ["button-box-bright-pressed", 70],
        backgroundColor : "button-box-dark-pressed"
      }
    },
    "button-box-hovered" :
    {
      include : "button-box",

      style :
      {
        gradientStart : ["#CCCCCC", 40],
        gradientEnd : ["#CCCCCC", 70],
        color : "button-border-hovered"
      }
    },
    "border-blue" :
    {
      decorator: qx.ui.decoration.Decorator,

      style :
      {
        width : 1,
        color : "background-selected"
      }
    },
    "window" :
    {
      decorator: [
        qx.ui.decoration.MDoubleBorder,
        qx.ui.decoration.MBoxShadow,
        qx.ui.decoration.MBackgroundColor
      ],

      style :
      {
        width : 1,
        color : "window-border",
        innerWidth : 1,
        innerColor: "window-border-inner",
        shadowLength : 1,
        shadowBlurRadius : 3,
        shadowColor : "shadow",
        backgroundColor : "background"
      }
    },
    "grid-header-cell-first" :
    {
      include : "grid-header-cell",
      style : {
        widthLeft : 1
      }
    },
    "grid-header-cell" :
    {
      decorator : qx.ui.decoration.Decorator,

      style :
      {
        widthRight : 1,
        color : "button-border"
      }
    },

    /*
    ---------------------------------------------------------------------------
      TEXT FIELD
    ---------------------------------------------------------------------------
    */
    "inset" :
    {
      style :
      {
        width : 1,
        radius: 3,
        color : [ "border-light-shadow", "border-light", "border-light", "border-light" ]
      }
    },

    "focused-inset" :
    {
      style :
      {
        width : 2,
        radius: 3,
        color : "background-selected"
      }
    }


	}
});
