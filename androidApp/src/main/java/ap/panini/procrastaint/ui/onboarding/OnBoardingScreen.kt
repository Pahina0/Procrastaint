package ap.panini.procrastaint.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.ui.components.ScreenScaffold
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
) {
    val pages = OnBoardingPage.entries.toList()

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    val preferenceRepository: PreferenceRepository = koinInject()


    ScreenScaffold {
        HorizontalPager(pagerState) { pageNum ->
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(title = { Text(pages[pageNum].topBarText) })
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                        ) {
                            pages[pageNum].content()
                        }

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {

                                coroutineScope.launch {

                                    if (pageNum == pagerState.pageCount - 1) {
                                        preferenceRepository.putBoolean(
                                            PreferenceRepository.ON_BOARDING_COMPLETE,
                                            true
                                        )
                                    } else {
                                        pagerState.scrollToPage(pageNum + 1)
                                    }

                                }
                            }
                        ) {
                            Text(pages[pageNum].completeText)
                        }
                    }
                }
            }
        }
    }
}
