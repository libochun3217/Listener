package li.songe.gkd.ui.share

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import li.songe.gkd.MainViewModel
import li.songe.gkd.store.blockMatchAppListFlow
import li.songe.gkd.util.AppSortOption

fun BaseViewModel.useAppFilter(
    sortTypeFlow: StateFlow<AppSortOption>,
    appOrderListFlow: StateFlow<List<String>> = MainViewModel.instance.appOrderListFlow,
    showBlockAppFlow: StateFlow<Boolean>? = null,
    blockAppListFlow: StateFlow<Set<String>> = blockMatchAppListFlow,
): AppFilter {

    val searchStrFlow = MutableStateFlow("")
    val debounceSearchStrFlow = searchStrFlow.debounce(200)
        .stateInit(searchStrFlow.value)
    val appActionOrderMapFlow = appOrderListFlow.map {
        it.mapIndexed { i, appId -> appId to i }.toMap()
    }


    return AppFilter(
        searchStrFlow = searchStrFlow,
    )
}


class AppFilter(
    val searchStrFlow: MutableStateFlow<String>,
)
