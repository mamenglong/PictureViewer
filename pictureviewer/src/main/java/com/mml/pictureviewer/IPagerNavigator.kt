package com.mml.pictureviewer

import net.lucode.hackware.magicindicator.NavigatorHelper
import net.lucode.hackware.magicindicator.abs.IPagerNavigator

interface IPagerNavigator<T>: IPagerNavigator, NavigatorHelper.OnNavigatorScrollListener {
    fun setDataSet (list:T)
    fun setItemClickListener (onItemClickListener:OnItemClickListener)


}

 interface OnItemClickListener {
    fun onClick(index:Int)
}
