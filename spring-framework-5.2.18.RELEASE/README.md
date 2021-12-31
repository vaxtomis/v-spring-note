#spring-framework
##容器
###1. 容器启动
1. 通过某种途径加载 Configuration MetaData  
(大部分情况下需要依赖 BeanDefinitionReader 对 MetaData 进行解析和分析)  
2. 将分析后的信息编组为相应的 BeanDefinition  
3. 把保存了 Bean 定义的 BeanDefinition 注册到 BeanDefinitionRegistry  
================================================================  
借助 BeanFactoryPostProcessor 来干预 Magic 实现的第一阶段。  
可以阅读的相关类包括（不完全）：  
BeanDefinition, FactoryBean, BeanFactoryPostProcessor,  
BeanFactoryPostProcessor, BeanFactoryAware, MethodReplacer,  
CustomEditorConfigurer,   
StringArrayPropertyEditor, FileEditor, LocaleEditor, PatternEditor;  
  
###2. Bean实例化阶段
当某个请求方通过容器的 getBean 方法明确地请求某个对象，或者因依赖关系容器需要隐式调用 getBean 时触发实例化阶段    
1. 检查所请求的对象之前是否已经初始化  
2. 若没有，则根据注册的 BeanDefinition 所提供的信息实例化被请求的对象  
（如果该对象实现了某些回调接口，也会根据回调接口的要求来装配它）  
3. 当该对象装配完毕之后，容器会立即将其返回请求方使用  
  
ApplicationContext 启动之后会实例化所有的 bean 定义，  
但 ApplicationContext 在实现的过程中依然遵循 Spring 容器实现流程的两个阶段。  
在启动阶段的活动完成之后，紧接着调用注册到该容器的所有 bean 定义的实例化方法 getBean()。  
相关方法：{AbstractApplicationContext#refresh};  

>#### Bean 实例化过程
>* 实例化 Bean 对象
>* 设置对象属性
>* 检查 Aware 相关接口并设置相关依赖（实例化完成且相关属性以及依赖设置完成后）
>* BeanPostProcessor（Aware 检测到则通过 BeanPostProcessor）
>* 检查是否是 InitializingBean 以决定是否调用 afterPropertiesSet 方法
>* 检查是否配置有自定义的 init-method（类似 afterPropertiesSet，耦合性更低）
>* BeanPostProcessor 后置处理
>* 注册必要的 Destruction 相关回调接口
>* ---使用中---
>* 是否实现 DisposableBean 接口（对应 InitializingBean）
>* 是否配置有自定义 destroy-method（对应 init-method）
>  
>相关阅读:{AbstractBeanFactory#getBean},
>{AbstractAutowireCapableBeanFactory#createBean};
  
