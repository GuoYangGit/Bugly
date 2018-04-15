# Bugly
关于腾讯Bugly的一些集成测试总结
> 前段时间项目上线因为http请求没进行加密，导致app又重新打包再次上线(Ps:我们项目上线有18个渠道)，当时渠道看我的眼神都变了，故此本人下定决心在下个版本上线前打算一定要接入热修复，这篇文章记录下我所接入bugly官网的tinker热修复配合walle多渠道打包的经历与坑。

### 为何接入tinker

起因是因为原先网上看到的android各大热修复对比，发现相比较AndFix，QQ空间	，Robust，Tinker的功能还是比较全面的，唯一不好的一点可能就是不能及时生效，需要重新启动吧。这里就不具体介绍tinker热修复的原理了，具体请看[微信Android热补丁实践演进之路
](https://mp.weixin.qq.com/s?__biz=MzAwNDY1ODY2OQ==&mid=2649286306&idx=1&sn=d6b2865e033a99de60b2d4314c6e0a25#rd)

顺便上一张对比图

![image](http://upload-images.jianshu.io/upload_images/3347923-e4a98b346a34f63e?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

其实前段时间阿里出来的[Sophix](https://www.aliyun.com/product/hotfix)更加强大，只是需要收费，直接pass不考虑好吧。
选择通过bugly来集成tinker是因为我们后台也很忙，应该没有时间配合我来管理patch(差分)包。而且我们的bug统计也是接入的bugly(这算不算是打了一波广告)。好了不多说了，直接步入正题吧。

### 接入tinker

1. 我们先去[bugly](https://bugly.qq.com/v2/index)创建项目，获取所需要的appkey。
2. 引入依赖

	```
	//项目根目录的build.gradle
	buildscript {
	    dependencies {
	       ...
	       // 只需配置tinker-support插件依赖，无需再依赖tinker插件
	       classpath 'com.tencent.bugly:tinker-support:1.1.1'
	    }
       }

	//app module的build.gradle
	// 依赖插件脚本，平时编写代码的时候这里其实可以注释，不然每次build都会生成基准包，只要在打正式包的时候放开注释就好了
	apply from: 'tinker-support.gradle'
	
	android {
	    ...
	    defaultConfig {
	        ndk {
	            //设置支持的SO库架构
	            abiFilters 'armeabi' //, 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
	        }
	    }
	}
	
	dependencies {
	    compile fileTree(dir: 'libs', include: ['*.jar'])
	    ...
	   //引入bugly的基础库
	    compile 'com.tencent.bugly:crashreport_upgrade:1.3.4'//其中latest.release指代最新版本号，也可以指定明确的版本号，例如1.2.0
	    compile 'com.tencent.bugly:nativecrashreport:3.3.1' //其中latest.release指代最新版本号，也可以指定明确的版本号，例如2.2.0
	}
	```

3. 创建tinker-support.gradle文件
	
	```
	apply plugin: 'com.tencent.bugly.tinker-support'
	//这里是基准包生成的路径。 每次build在app/build/bakApk下会生成我们的基准包
	def bakPath = file("${buildDir}/bakApk/")
	
	/**
	 * 此处填写每次构建生成的基准包目录
	 * 这里的基准包需要在生成patch包的时候填写，平时不用管
	 */
	def baseApkDir = "v1.0.1"
	
	/**
	 * 对于插件各参数的详细解析请参考
	 */
	tinkerSupport {
	
	    // 开启tinker-support插件，默认值true
	    // 这里我们平时编写代码的时候同样可以不用设置成true
	    enable = true
	
	    // 这里就是我们每次build的时候生成的基准包目录
	    // 指向我们上面定义的def bakPath
	    autoBackupApkDir = "${bakPath}"
	
	    // 自动生成tinkerId, 你无须关注tinkerId，默认为false
	    autoGenerateTinkerId = true
	
	    // 是否启用覆盖tinkerPatch配置功能，默认值false
	    // 开启后tinkerPatch配置不生效，即无需添加tinkerPatch
	    overrideTinkerPatchConfiguration = true
	
	    // 编译补丁包时，必需指定基线版本的apk，默认值为空
	    // 如果为空，则表示不是进行补丁包的编译
	    // @{link tinkerPatch.oldApk }
	    baseApk =  "${bakPath}/${baseApkDir}/app-release.apk"
	
	    // 对应tinker插件applyMapping
	    // 这里是我们开启混淆的时候生成的混淆文件
	    baseApkProguardMapping = "${bakPath}/${baseApkDir}/app-release-mapping.txt"
	
	    // 对应tinker插件applyResourceMapping
	    // 这里是我们生成的R(资源)文件
	    baseApkResourceMapping = "${bakPath}/${baseApkDir}/app-release-R.txt"
	
	    // 构建基准包跟补丁包都要修改tinkerId，主要用于区分,例如:base-1.0.1  patch-1.0.1
	    tinkerId = "patch1-1.0.1"
	
	    // 是否开启加固模式，默认为false
	    // 因为我们渠道有360渠道，所以需要加固
	     isProtectedApp = true
	
	    // 是否采用反射Application的方式集成，这样我们就不需要再去使用我们的application继承tinker的application	    enableProxyApplication = true
	
	    // 是否支持新增非export的Activity（注意：设置为true才能修改AndroidManifest文件）
	    supportHotplugComponent = true
	}
	
	/**
	 * 一般来说,我们无需对下面的参数做任何的修改
	 * 对于各参数的详细介绍请参考:
	 * https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97
	 */
	tinkerPatch {
	    //oldApk ="${bakPath}/${appName}/app-release.apk"
	    ignoreWarning = false
	    useSign = true
	    dex {
	        dexMode = "jar"
	        pattern = ["classes*.dex"]
	        loader = []
	    }
	    lib {
	        pattern = ["lib/*/*.so"]
	    }
	
	    res {
	        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
	        ignoreChange = []
	        largeModSize = 100
	    }
	
	    packageConfig {
	    }
	    sevenZip {
	        zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
	//        path = "/usr/local/bin/7za"
	    }
	    buildConfig {
	        keepDexApply = false
	        //tinkerId = "1.0.1-base"
	        //applyMapping = "${bakPath}/${appName}/app-release-mapping.txt" //  可选，设置mapping文件，建议保持旧apk的proguard混淆方式
	        //applyResourceMapping = "${bakPath}/${appName}/app-release-R.txt" // 可选，设置R.txt文件，通过旧apk文件保持ResId的分配
	    }
	}
	```
	
4. application中初始化SDK

	```
	class MyApplication : Application() {
	    override fun onCreate() {
	        super.onCreate()
	        /**
	         * 已经接入Bugly用户改用下面的初始化方法,不影响原有的crash上报功能;
	         * init方法会自动检测更新，不需要再手动调用Beta.checkUpdate(),如需增加自动检查时机可以使用Beta.checkUpdate(false,false);
	         * 参数1：applicationContext
	         * 参数2：appId
	         * 参数3：是否开启debug
	         */
	        Bugly.init(applicationContext, "这里是我们bugly上面申请的appId", true)
	    }
	
	    override fun attachBaseContext(base: Context) {
	        super.attachBaseContext(base)
	        MultiDex.install(base)
	        // 安装tinker
	        Beta.installTinker()
	    }
	}
	```
	
### 接入walle
	
因为我们的项目每次打包需要打18个渠道包(我也不知道为啥打那么多渠道)，而且原来我们是使用productFlavors来打包的，打包速度可想而之。而且bugly不推荐这种方式打包(我们不可能为每个渠道包都去打一个patch包，如果这样18个patch包反正我是要疯了)。废话不多说，我们直接接入walle吧。

1. 引入依赖

	```
	//项目根目录的build.gradle
	buildscript {
	    dependencies {
	       ...
	       // 只需配置tinker-support插件依赖，无需再依赖tinker插件
	       classpath 'com.tencent.bugly:tinker-support:1.1.1'
	       //walle多渠道打包
	       classpath 'com.meituan.android.walle:plugin:1.1.6'
	    }
  	}

	//app module的build.gradle
	// 依赖插件脚本，平时编写代码的时候这里其实可以注释，不然每次build都会生成基准包，只要在打正式包的时候放开注释就好了
	apply from: 'tinker-support.gradle'
	//walle 多渠道打包
	apply plugin: 'walle'
	android {
	    ...
	    defaultConfig {
	        ndk {
	            //设置支持的SO库架构
	            abiFilters 'armeabi' //, 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
	        }
	    }
	}
	
	dependencies {
	    compile fileTree(dir: 'libs', include: ['*.jar'])
	    ...
	   //引入bugly的基础库
	    compile 'com.tencent.bugly:crashreport_upgrade:1.3.4'//其中latest.release指代最新版本号，也可以指定明确的版本号，例如1.2.0
	    compile 'com.tencent.bugly:nativecrashreport:3.3.1' //其中latest.release指代最新版本号，也可以指定明确的版本号，例如2.2.0
	    //引入walle多渠道的依赖
	    compile "com.meituan.android.walle:library:1.1.6"
	}
	
	/**
	 * 生成渠道包: ./gradlew clean assembleReleaseChannels
	 * 生成单个渠道包: ./gradlew clean assembleReleaseChannels -PchannelList=xiaomi
	 * 生成多个渠道包: ./gradlew clean assembleReleaseChannels -PchannelList= xiaomi,huawei
	 * 具体用法: https://github.com/Meituan-Dianping/walle
	 */
	walle {
	    //指定渠道包的输出路径
	    apkOutputFolder = new File("${project.buildDir}/outputs/channels")
	    //定制渠道包的APK的文件名称 例如app-xiaomi.apk
	    apkFileNameFormat = 'app-${channel}.apk'
	    //渠道配置文件
	    channelFile = new File("${project.getProjectDir()}/channel")
	}
	```
	
2. app目录下创建编写channel文件

	```
	#表示注释
	xiaomi #小米
	ali #阿里
	360 #360
	```
	
3. 在application中写入渠道

	```
	class MyApplication : Application() {
	    override fun onCreate() {
	        super.onCreate()
	        //通过walle获取渠道信息
	        val channel = WalleChannelReader.getChannel(this,"default")
	        //bugly写入渠道信息
	        Bugly.setAppChannel(this, channel)
	        /**
	         * 已经接入Bugly用户改用下面的初始化方法,不影响原有的crash上报功能;
	         * init方法会自动检测更新，不需要再手动调用Beta.checkUpdate(),如需增加自动检查时机可以使用Beta.checkUpdate(false,false);
	         * 参数1：applicationContext
	         * 参数2：appId
	         * 参数3：是否开启debug
	         */
	        Bugly.init(applicationContext, "这里是我们bugly上面申请的appId", true)
	    }
	
	    override fun attachBaseContext(base: Context) {
	        super.attachBaseContext(base)
	        MultiDex.install(base)
	        // 安装tinker
	        Beta.installTinker()
	    }
	}
	```
	
### 正式测试

1. 我们先来写一段bug(Ps:对于写bug我还是在行的,开个玩笑)

	```
	class MainActivity : AppCompatActivity() {
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	        //获取我们写入的渠道信息
	        val channel = WalleChannelReader.getChannel(this,"default")
	        //赋值给textview显示
	        channel_tv.text = channel
	        //这里我们写一个点击事件，点击tosat
	        test_btn.setOnClickListener {
	            toast("${2/0}")
	        }
	    }
	}
	```
	接下来我们去tinker-support.gradle中修改一丢丢东西
        ![bug基准包.png](https://github.com/GuoYangGit/Bugly/blob/master/png/bug%E5%9F%BA%E5%87%86%E5%8C%85.png)

	然后我们在Terminal中运行`./gradlew clean assembleReleaseChannels`来进行多渠道打包，漫长的50s等待...
	打包完成，我们来看一下生成目录
	![bug包目录.png](https://github.com/GuoYangGit/Bugly/blob/master/png/bug%E5%8C%85%E7%9B%AE%E5%BD%95.png)

	我们将ali的包装到手机上运行一下，不出意外的话就会崩溃。
	
	![bug.gif](https://github.com/GuoYangGit/Bugly/blob/master/gif/bug.gif)
	
2. 进行bug修改

	```
	class MainActivity : AppCompatActivity() {
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	        //获取我们写入的渠道信息
	        val channel = WalleChannelReader.getChannel(this,"default")
	        //赋值给textview显示
	        channel_tv.text = channel
	        //这里我们写一个点击事件，点击tosat
	        test_btn.setOnClickListener {
	        	  //我们将这里的bug修复一下
	            toast("${2/1}")
	        }
	    }
	}
	```
	
	接下来我们还是去tinker-support.gradle中修改一丢丢东西
	![patch包.png](https://github.com/GuoYangGit/Bugly/blob/master/png/patch%E5%8C%85.png)
	
	然后去生成patch包
	![生成patch包.png](https://github.com/GuoYangGit/Bugly/blob/master/png/%E7%94%9F%E6%88%90patch%E5%8C%85.png)
	
	找到生成的patch包上传bugly进行修复
	![上传patch包.png](https://github.com/GuoYangGit/Bugly/blob/master/png/%E4%B8%8A%E4%BC%A0patch%E5%8C%85.png)
	
	接下来我们看看修复后的效果
	
        ![修复后.gif](https://github.com/GuoYangGit/Bugly/blob/master/gif/%E4%BF%AE%E5%A4%8D%E5%90%8E.gif)
	我们看到bug已经被修复了，关于360加固/乐固这里我就不测试了。
	说一下流程吧，我们先去360官网或者加固工具进行加固，然后使用这个[python脚本](https://github.com/Jay-Goo/ProtectedApkResignerForWalle)进行签名与渠道信息写入，具体流程github说的已经很详细了。
	
### 所注意事项


1. 我们生成的基准包目录，例如我的v1.0.1这个包(包括里面的apk，R文件，以及混淆文件)一定要保存好，因为我们以后每次发现bug都需要基于这个包去生成patch包。
2. 我们平时在编码期间可以注释掉bugly不让它每次build都去生成基准包。
3. 每次生成基准包和patch包的时候一定要去tinker-support.gradle里面修改tinkerId，而且最好自己定义一个规则区分一下版本以及基准包和patch包。
4. 生成patch包的时候tinker-support.gradle文件里面的def baseApkDir一定要指向我们所要修复的基准包，顺便检查下baseApk所指向的apk名字，baseApkProguardMapping所指向的混淆文件名，baseApkResourceMapping所指向的R文件名字。
5. 这里其实我有个问题，如果我要开启加固模式，那么对没有加固的apk修复到底有没有影响，因为我这里没有加固也可以修复，哪位大佬知道可以留言说下。

---
好吧，今天的文章就写到这里吧，明天就要上班了。
[项目地址](https://github.com/GuoYangGit/Bugly)
