#spring-framework
##容器
###1. 容器启动
1. 通过某种途径加载 Configuration MetaData  
(大部分情况下需要依赖 BeanDefinitionReader 对 MetaData 进行解析和分析)  
2. 将分析后的信息编组为相应的 BeanDefinition  
3. 把保存了 Bean 定义的 BeanDefinition 注册到 BeanDefinitionRegistry  
###2. Bean实例化阶段
当某个请求方通过容器的 getBean 方法明确地请求某个对象，  或者因依赖关系容器需要隐式调用 getBean 时触发实例化阶段    
1. 检查所请求的对象之前是否已经初始化  
2. 若没有，则根据注册的 BeanDefinition 所提供的信息实例化被请求的对象  
（如果该对象实现了某些回调接口，也会根据回调接口的要求来装配它）  
3. 当该对象装配完毕之后，容器会立即将其返回请求方使用  