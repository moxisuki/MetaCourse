@file:OptIn(ExperimentalMaterial3Api::class)

package app.linw.metacourse

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.linw.metacourse.data.MainTabs
import app.linw.metacourse.ui.theme.MetaCourseTheme
import app.linw.metacourse.utils.CourseUtil
import kotlinx.coroutines.launch
import java.util.Calendar

const val TAG = "MetaCourseDebug"

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            MetaCourseTheme {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MainNavigationBar(navController) },
                    topBar = { TopBar() }) {

                    Column {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        ) {
                            DataCenter(navController, snackbarHostState)
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )
        }, modifier = Modifier.padding(10.dp)
    )
}

@Composable
fun MainNavigationBar(navController: NavHostController) {
    val items = listOf(
        MainTabs.HOME,
        MainTabs.INFO,
    )
    NavigationBar(containerColor = Color.Transparent) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEachIndexed { index, item ->
            NavigationBarItem(icon = { Icon(item.icon, contentDescription = item.route) },
                label = { Text(item.route) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
        }
    }
}


@Composable
fun DataCenter(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    var timeDataAM = remember {
        mutableStateListOf(
            mutableStateMapOf(
                "start_time" to "07:05", "end_time" to "07:37"
            )
        )
    }
    var timeDataPM = remember {
        mutableStateListOf(
            mutableStateMapOf(
                "start_time" to "13:00", "end_time" to "14:00"
            )
        )
    }
    var timeDataNT = remember {
        mutableStateListOf(
            mutableStateMapOf(
                "start_time" to "18:00", "end_time" to "18:30"
            )
        )
    }



    NavHost(
        navController = navController,
        startDestination = MainTabs.HOME.route
    ) {
        composable(MainTabs.HOME.route) {
            Home(
                snackbarHostState,
                timeDataAM,
                timeDataPM,
                timeDataNT
            )
        }
        composable(MainTabs.INFO.route) { Info(
            timeDataAM,
            timeDataPM,
            timeDataNT) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    snackbarHostState: SnackbarHostState?,
    timeDataAM: SnapshotStateList<SnapshotStateMap<String, String>>,
    timeDataPM: SnapshotStateList<SnapshotStateMap<String, String>>,
    timeDataNT: SnapshotStateList<SnapshotStateMap<String, String>>
) {

    var selectedTag = remember {
        mutableStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
    ) {

        TimeTable("上午", timeDataAM, snackbarHostState, remember {
            mutableStateMapOf(
                "start_time" to "07:05", "end_time" to "07:37"
            )
        })
        TimeTable("下午", timeDataPM, snackbarHostState, remember {
            mutableStateMapOf(
                "start_time" to "13:00", "end_time" to "14:00"
            )
        })
        TimeTable("晚上", timeDataNT, snackbarHostState, remember {
            mutableStateMapOf(
                "start_time" to "18:00", "end_time" to "18:30"
            )
        })

    }
}

@Composable
fun Info(
    timeDataAM: SnapshotStateList<SnapshotStateMap<String, String>>,
    timeDataPM: SnapshotStateList<SnapshotStateMap<String, String>>,
    timeDataNT: SnapshotStateList<SnapshotStateMap<String, String>>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(start = 40.dp, end = 40.dp, top = 15.dp)
                .height(100.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxSize()
            ) {
                val days = remember {
                    mutableStateListOf<String>(
                        "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"
                    )
                }
                SelectedTextField(days, "选择一星期中的一天")
            }
        }
        EditorPanel(
            timeDataAM,
            timeDataPM,
            timeDataNT)
    }
}


@Composable
fun TimeTable(
    label: String,
    timeDataAM: SnapshotStateList<SnapshotStateMap<String, String>>,
    snackbarHostState: SnackbarHostState?,
    defaultTime: SnapshotStateMap<String, String>
) {
    var isDeletedMode = remember {
        mutableStateOf(false)
    }
    Column() {
        Card(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(start = 40.dp, end = 40.dp, top = 15.dp)
                .height(200.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Row(
                    modifier = Modifier.weight(0.15F)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(0.5F)
                            .align(CenterVertically)
                    )
                    Row(modifier = Modifier.weight(0.2F)) {
                        IconButton(onClick = {
                            val newTime =
                                if (timeDataAM.isEmpty()) defaultTime else CourseUtil.getTimeIntervalAfter(
                                    timeDataAM.last(), 40
                                )
                            timeDataAM.add(newTime)
                            Log.e(TAG, newTime.toString())
                            Log.e(TAG, "ok")
                        }) {
                            Icon(Icons.Default.Add, "Add")
                        }
                        IconButton(onClick = {
                            isDeletedMode.value = !isDeletedMode.value
                        }) {
                            Icon(
                                if (isDeletedMode.value) Icons.Default.Check else Icons.Default.Close,
                                "Close"
                            )
                        }
                    }
                }
                var size = remember {
                    mutableStateOf(timeDataAM.size)
                }
                LazyColumn(Modifier.weight(0.85F)) {
                    itemsIndexed(timeDataAM) { i, item ->
                        TimeItem(isDeletedMode, snackbarHostState, item) {
                            timeDataAM.also { it.removeAt(i) }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TimeItem(
    isDeletedMode: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState?,
    timeItem: SnapshotStateMap<String, String>,
    delete: () -> Unit
) {
    // Build a time picker
    val mContext = LocalContext.current
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar[Calendar.HOUR_OF_DAY]
    val mMinute = mCalendar[Calendar.MINUTE]

    val scope = rememberCoroutineScope()
    val mTime = remember { mutableStateOf(timeItem["start_time"].toString()) }
    val mTime2 = remember { mutableStateOf(timeItem["end_time"].toString()) }
    val mTimePickerDialog = TimePickerDialog(
        mContext, { _, mHour: Int, mMinute: Int ->
            mTime.value =
                "${if (mHour < 10) "0$mHour" else mHour}:${if (mMinute < 10) "0$mMinute" else mMinute}"
            if (CourseUtil.compareStringTimes(mTime.value, mTime2.value)) {
                mTime.value = "00:00"
                scope.launch {
                    snackbarHostState?.showSnackbar(
                        "右侧时间应晚于左侧", null, false, SnackbarDuration.Short
                    )
                }
            }
        }, mHour, mMinute, true
    )
    val mTimePickerDialog2 = TimePickerDialog(
        mContext, { _, mHour: Int, mMinute: Int ->
            mTime2.value =
                "${if (mHour < 10) "0$mHour" else mHour}:${if (mMinute < 10) "0$mMinute" else mMinute}"
            if (CourseUtil.compareStringTimes(mTime.value, mTime2.value)) {
                mTime2.value = "23:59"
                scope.launch {
                    snackbarHostState?.showSnackbar(
                        "右侧时间应晚于左侧", null, false, SnackbarDuration.Long
                    )
                }
            }
        }, mHour, mMinute, true
    )
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        TextButton(onClick = {
            mTimePickerDialog.show()
        }, modifier = Modifier.weight(0.4F)) {
            Text(text = mTime.value, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = {
            if (isDeletedMode.value) {
                delete()
            }
        }) {
            Icon(
                imageVector = if (!isDeletedMode.value) Icons.Default.ArrowForward else Icons.Default.Delete,
                contentDescription = "To",
                Modifier
                    .weight(0.2F)
                    .fillMaxHeight()
                    .align(CenterVertically)
                    .padding(10.dp)
            )
        }

        TextButton(onClick = {
            mTimePickerDialog2.show()
        }, modifier = Modifier.weight(0.4F)) {
            Text(text = mTime2.value, style = MaterialTheme.typography.bodyLarge)
        }
    }
    Divider()
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditorPanel(
    timeDataAM: SnapshotStateList<SnapshotStateMap<String, String>>,
    timeDataPM: SnapshotStateList<SnapshotStateMap<String, String>>,
    timeDataNT: SnapshotStateList<SnapshotStateMap<String, String>>
) {
    val courseData = remember {
        mutableStateListOf(
            mutableStateMapOf("name" to "语文", "teacher" to "朱老师", "room" to "教室"),
            mutableStateMapOf("name" to "语文", "teacher" to "朱老师", "room" to "教室"),
            mutableStateMapOf("name" to "语文", "teacher" to "朱老师", "room" to "教室")
        )
    }
    var selectedTag = remember {
        mutableStateOf(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 40.dp, top = 20.dp)
    ) {
        Card(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp)
                    .align(Alignment.CenterHorizontally)
                    .weight(0.5F)
            ) {
                if (selectedTag.value > -1) {
                    TextField(
                        label = {
                            Text(text = "课程名称")
                        },
                        value = courseData[selectedTag.value]["name"].orEmpty(),
                        onValueChange = {
                            courseData[selectedTag.value]["name"] = it

                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        label = {
                            Text(text = "课程老师")
                        },
                        value = courseData[selectedTag.value]["teacher"].orEmpty(),
                        onValueChange = {
                            courseData[selectedTag.value]["teacher"] = it
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        label = {
                            Text(text = "上课教室")
                        },
                        value = courseData[selectedTag.value]["room"].orEmpty(),
                        onValueChange = {
                            courseData[selectedTag.value]["room"] = it
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val classes = mutableListOf<String>()

                    val data = CourseUtil.generateCourseLabel(
                        timeDataAM,
                        "AM"
                    ) + CourseUtil.generateCourseLabel(
                        timeDataPM,
                        "PM"
                    ) + CourseUtil.generateCourseLabel(timeDataNT, "NT")

                    var timeLabels = remember {
                        mutableStateOf(data)
                    }
                    var labels = remember{
                        mutableStateListOf<String>()
                    }
                    // TODO 两个页面之间数据交互
                    SelectedTextField(labels, "课程位置")
                } else {
                    Text(
                        text = "请先选择一个课时",
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.5F)
                    .padding(15.dp)
            ) {
                Row(

                    modifier = Modifier.weight(0.15F)
                ) {
                    Text(
                        text = "Courses",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(0.5F)
                            .align(CenterVertically)
                    )
                    Row(modifier = Modifier.weight(0.5F)) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.Add, "Add")
                        }
                        IconButton(onClick = {
                            if (courseData.size >= selectedTag.value && selectedTag.value > -1) {
                                courseData.removeAt(selectedTag.value)
                                selectedTag.value--
                            }
                        }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.MoreVert, "MoreVert")
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxSize()
                ) {
                    CourseList(courseData, selectedTag)
                }
            }

        }
    }
}


@Composable
fun CourseList(
    courseData: SnapshotStateList<SnapshotStateMap<String, String>>, selectedTag: MutableState<Int>
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(5.dp)
    ) {

        items(courseData.size) {
            TextButton(onClick = {
                selectedTag.value = it
            }) {
                RadioButton(selected = it == selectedTag.value,
                    modifier = Modifier.size(20.dp),
                    onClick = {
                        selectedTag.value = it
                    })
                Text(
                    courseData[it]["name"].orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Divider(
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedTextField(options:  SnapshotStateList<String>, label: String) {
    var expanded = remember {
        mutableStateOf(false)
    }
    var week = remember {
        mutableStateOf<String>(options[0])
    }

    OutlinedTextField(label = { Text(text = label) },
        readOnly = true,
        value = week.value,
        onValueChange = {},
        trailingIcon = {
            IconButton(onClick = {
                expanded.value = true
                Log.e(TAG, "Okkk")
            }) {
                Icon(Icons.Default.ArrowDropDown, "选择")
            }
        })
    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        options.forEach { day ->
            DropdownMenuItem(text = { Text(text = day) }, onClick = {
                week.value = day
                expanded.value = false
            })
        }
    }
}


@Preview(showBackground = true, device = Devices.PIXEL_3)
@Composable
fun MainPagePreview() {
    MetaCourseTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        var timeDataAM = remember {
            mutableStateListOf(
                mutableStateMapOf(
                    "start_time" to "07:05", "end_time" to "07:37"
                )
            )
        }
        var timeDataPM = remember {
            mutableStateListOf(
                mutableStateMapOf(
                    "start_time" to "13:00", "end_time" to "14:00"
                )
            )
        }
        var timeDataNT = remember {
            mutableStateListOf(
                mutableStateMapOf(
                    "start_time" to "18:00", "end_time" to "18:30"
                )
            )
        }
        Home(snackbarHostState, timeDataAM, timeDataPM, timeDataNT)
    }
}