package com.rhyme.sidekick.andropilot.context

import com.rhyme.sidekick.andropilot.config.CLIConfigImpl
import com.rhyme.sidekick.andropilot.config.CLIResourcesImpl
import com.rhyme.sidekick.andropilot.data.local.preferences.CLIPreferencesImpl
import com.rhyme.sidekick.cmdx.framework.CLIContextImpl
import com.rhyme.sidekick.cmdx.framework.CLIPreferences
import com.rhyme.sidekick.cmdx.framework.CLIServiceManager
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator
import com.rhyme.sidekick.cmdx.room.runtime.CLIRoomDatabase
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.cmdx.servicemanager
 * 描述：CLIContext功能测试
 *
 * @author 李春俊-Rhyme
 * @since 21:10 2026/5/6 星期三
 * @version 1.0
 */
class CLIContextTest {

    private fun createTestContext(): CLIContextImpl {
        return CLIContextImpl(
            config = CLIConfigImpl.load(),
            resources = CLIResourcesImpl(
                stringsFile = Paths.get("strings.json"),
                iconsFile = Paths.get("icons.json"),
                config = CLIConfigImpl.load()
            ),
            preferences = CLIPreferencesImpl(),
            database = object : CLIRoomDatabase() {},
            navigator = CLINavigator("home"),
            lifecycleOwner = CLILifecycleOwner(),
            viewModelStoreOwner = CLIViewModelStoreOwner(),
            serviceManager = CLIServiceManager(),
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )
    }

    @Test
    fun testGetServiceWithReified() {
        val context = createTestContext()

        // 注册服务（使用reified版本）
        val testService = TestServiceImpl()
        context.registerService<TestService>(testService)

        // 获取服务（使用reified版本）
        val retrievedService = context.getService<TestService>()

        assertNotNull(retrievedService)
        assertEquals(testService, retrievedService)
    }

    @Test
    fun testInjectWithReified() {
        val context = createTestContext()

        // 注册服务（使用reified版本）
        val testService = TestServiceImpl()
        context.registerService<TestService>(testService)

        // 注入服务（懒加载，使用reified版本）
        val lazyService by context.inject<TestService>()

        // 此时还未访问，但应该能正常工作
        assertNotNull(lazyService)
        assertEquals("test", lazyService.execute())
    }

    @Test
    fun testGetServiceNotRegistered() {
        val context = createTestContext()

        // 尝试获取未注册的服务应该抛出异常
        assertFailsWith<IllegalArgumentException> {
            context.getService<TestService>()
        }
    }

    @Test
    fun testServiceFactory() {
        val context = createTestContext()

        // 注册服务工厂
        var creationCount = 0
        context.registerServiceFactory<TestService> {
            creationCount++
            TestServiceImpl()
        }

        // 第一次获取时创建实例
        val service1 = context.getService(TestService::class)
        assertEquals(1, creationCount)

        // 第二次获取时应该返回同一个实例（单例）
        val service2 = context.getService(TestService::class)
        assertEquals(1, creationCount) // 不应该再次创建
        assertEquals(service1, service2)
    }

}

// ==================== 测试用的服务接口和实现 ====================
interface TestService {
    fun execute(): String
}

class TestServiceImpl : TestService {
    override fun execute(): String {
        return "test"
    }
}

interface AnotherTestService {
    fun execute(): String
}

class AnotherTestServiceImpl : AnotherTestService {
    override fun execute(): String {
        return "another"
    }
}