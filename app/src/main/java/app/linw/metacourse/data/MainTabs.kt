package app.linw.metacourse.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector


sealed class MainTabs(val route: String, val icon: ImageVector) {
    object HOME : MainTabs("课时信息", Icons.Filled.Home)
    object INFO : MainTabs("课程配置", Icons.Filled.Info)
}