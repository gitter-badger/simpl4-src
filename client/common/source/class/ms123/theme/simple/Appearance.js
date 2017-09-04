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
qx.Theme.define("ms123.theme.simple.Appearance",
{
  extend : qx.theme.simple.Appearance,

  appearances :
  {
    "window/captionbar" :
    {
      style : function(states)
      {
        return {
          backgroundColor : states.active ? "light-background" : "background-disabled",
          padding : 3,
          font: "bold",
          decorator : "window-caption"
        };
      }
    },
    "toolbar-button" :
    {
      alias : "atom",

      style : function(states)
      {
        var decorator = "button-box";

        if (states.disabled) {
          decorator = "button-box";
        } else if (states.hovered && !states.pressed && !states.checked) {
          decorator = "button-box-hovered";
        } else if (states.hovered && (states.pressed || states.checked)) {
          decorator = "button-box-pressed-hovered";
        } else if (states.pressed || states.checked) {
          decorator = "button-box-pressed";
        }

        // set the right left and right decoratos
        if (states.left) {
          decorator += "-left";
        } else if (states.right) {
          decorator += "-right";
        } else if (states.middle) {
          decorator += "-middle";
        }

        // set the margin
        var margin = [0, 0];
        if (states.left || states.middle || states.right) {
          margin = [0, 0];
        }

        return {
          cursor  : states.disabled ? undefined : "pointer",
          decorator : decorator,
          margin : margin,
          padding: [1, 1]
        };
      }
    },
    "tabview-page/button" :
    {
      style : function(states)
      {
        var decorator;

        // default padding
        if (states.barTop || states.barBottom) {
          var padding = [4, 8, 4, 6];
        } else {
          var padding = [4, 2, 4, 2];
        }

        // decorator
        if (states.checked) {
          if (states.barTop) {
            decorator = "tabview-page-button-top";
          } else if (states.barBottom) {
            decorator = "tabview-page-button-bottom"
          } else if (states.barRight) {
            decorator = "tabview-page-button-right";
          } else if (states.barLeft) {
            decorator = "tabview-page-button-left";
          }
        } else {
          for (var i=0; i < padding.length; i++) {
            padding[i] += 1;
          }
          // reduce the size by 1 because we have different decorator border width
          if (states.barTop) {
            padding[2] -= 1;
          } else if (states.barBottom) {
            padding[0] -= 1;
          } else if (states.barRight) {
            padding[3] -= 1;
          } else if (states.barLeft) {
            padding[1] -= 1;
          }
        }

        return {
          zIndex : states.checked ? 10 : 5,
          decorator : decorator,
          textColor : states.disabled ? "text-disabled" : states.checked ? null : "link",
          padding : padding,
          cursor: "pointer"
        };
      }
    },
    "spinner/upbutton" :
    {
      alias : "combobox/button",
      include : "combobox/button",

      style : function(states)
      {
        var decorator = "button-box-top-right";

        if (states.hovered && !states.pressed && !states.checked) {
          decorator = "button-box-hovered-top-right";
        } else if (states.hovered && (states.pressed || states.checked)) {
          decorator = "button-box-pressed-hovered-top-right";
        } else if (states.pressed || states.checked) {
          decorator = "button-box-pressed-top-right";
        }

        return {
          icon : qx.theme.simple.Image.URLS["arrow-up-small"],
          decorator : decorator,
          width: 17,
					height:10
        }
      }
    },

    "spinner/downbutton" :
    {
      alias : "combobox/button",
      include : "combobox/button",

      style : function(states)
      {
        var decorator = "button-box-bottom-right";

        if (states.hovered && !states.pressed && !states.checked) {
          decorator = "button-box-hovered-bottom-right";
        } else if (states.hovered && (states.pressed || states.checked)) {
          decorator = "button-box-pressed-hovered-bottom-right";
        } else if (states.pressed || states.checked) {
          decorator = "button-box-pressed-bottom-right";
        }

        return {
          icon : qx.theme.simple.Image.URLS["arrow-down-small"],
          decorator : decorator,
          width: 17,
					height:10
        }
      }
    },
    "groupbox/legend" :
    {
      alias : "atom",

      style : function(states)
      {
        return {
          padding   : [1, 0, 1, 4],
          textColor : states.invalid ? "invalid" : "text-title",
          font      : "bold"
        };
      }
    },
    "collapsable-panel" : {
      style : function(states) {
        return {
          decorator  : "border-blue",
          padding    : 3,
          margin    : 2,
          allowGrowY : !!states.opened || !!states.horizontal,
          allowGrowX : !!states.opened ||  !states.horizontal
        };
      }
    },

    "collapsable-panel/bar" : {
      include : "groupbox/legend",
      alias   : "groupbox/legend",
      style   : function(states) {
        return {
          icon       :  states.opened ? "resource/ms123/open.png" : "resource/ms123/closed.png",
          allowGrowY : !states.opened && !!states.horizontal,
          allowGrowX :  states.opened ||  !states.horizontal,
          maxWidth   : !states.opened && !!states.horizontal ? 16 : null
        };
      }
    },

    "collapsable-panel/container" : {
      style : function(states) {
        return { padding : [1, 5] };
      }
    },

    "grid-header-cell" :
    {
      alias : "atom",

      style : function(states)
      {
        return {
          decorator : states.first ? "grid-header-cell-first" : "grid-header-cell",
          minWidth: 13,
          font : "bold",
          paddingTop: 3,
          paddingLeft: 5
        }
      }
    }
  }
});
