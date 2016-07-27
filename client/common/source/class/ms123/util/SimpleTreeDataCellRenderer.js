qx.Class.define("ms123.util.SimpleTreeDataCellRenderer", {
  extend : qx.ui.treevirtual.SimpleTreeDataCellRenderer,


  construct : function() {
    this.base(arguments);
  },


  statics : {
  },


  properties : {
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members : {
    _addIndentation : function(cellInfo, pos) {
      var node = cellInfo.value;
      var imageData;
      var html = "";

      // Generate the indentation.  Obtain icon determination values once
      // rather than each time through the loop.
      var bUseTreeLines = this.getUseTreeLines();
      var bExcludeFirstLevelTreeLines = this.getExcludeFirstLevelTreeLines();
      var bAlwaysShowOpenCloseSymbol = this.getAlwaysShowOpenCloseSymbol();

      for (var i=0; i<node.level; i++)
      {
        imageData = this._getIndentSymbol(i, node, bUseTreeLines,
                                          bAlwaysShowOpenCloseSymbol,
                                          bExcludeFirstLevelTreeLines);

        var rowHeight = cellInfo.table.getRowHeight();

        html += this._addImage(
        {
          url         : imageData.icon,
          position    :
          {
            top         : 4,
            left        : pos + (imageData.paddingLeft || 0),
            width       : rowHeight + 3,
            height      : rowHeight
          },
          imageWidth  : rowHeight,
          imageHeight : rowHeight
        });
        pos += rowHeight + 3;
      }

      return (
        {
          html : html,
          pos  : pos
        });
    },
    _addIcon : function(cellInfo, pos) {
      var node = cellInfo.value;

      // Add the node's icon
      var imageUrl = (node.bSelected ? node.iconSelected : node.icon);

      if (!imageUrl)
      {
        if (node.type == qx.ui.treevirtual.SimpleTreeDataModel.Type.LEAF)
        {
          var o = this.__tm.styleFrom("treevirtual-file");
        }
        else
        {
          var states = { opened : node.bOpened };
          var o = this.__tm.styleFrom("treevirtual-folder", states);
        }

        imageUrl = o.icon;
      }

      var rowHeight = cellInfo.table.getRowHeight();

      var html = this._addImage(
      {
        url         : imageUrl,
        position    :
        {
          top         : 4,
          left        : pos,
          width       : rowHeight + 3,
          height      : rowHeight
        },
        imageWidth  : rowHeight,
        imageHeight : rowHeight
      });

      return (
        {
          html : html,
          pos  : pos + rowHeight + 3
        });
    }
  },

  destruct : function() {
  }
});
