package net.neoforged.bus.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * NeoForge API 兼容层：反射式事件总线。
 * 扫描 @SubscribeEvent 方法（任意可见性，静态或实例），按 EventPriority 分发。
 *
 * <p>与 NeoForge 对齐的三条语义（都踩过坑，勿回退）：
 * <ul>
 *   <li><b>优先级顺序</b>：HIGHEST 最先、LOWEST 最后。{@link EventPriority} 的 index 是
 *       HIGHEST=0…LOWEST=4，所以排序键是 {@code +index} 的升序，不是 {@code -index}。</li>
 *   <li><b>沿继承链分发</b>：监听 {@code Foo} 的方法也必须收到 {@code Foo.Bar} 的投递，
 *       NeoForge 就是这么做的。只按 {@code event.getClass()} 精确匹配会让父类型监听器静默失效。</li>
 *   <li><b>注册顺序确定</b>：{@code Class#getDeclaredMethods} 不保证顺序，同优先级监听器的
 *       相对顺序会随 JVM 变化。这里按方法签名排序，保证两端（客户端/服务端）一致。</li>
 * </ul>
 */
public final class EventBus implements IEventBus {

    private record Handler(Object instance, Class<?> owner, Method method, EventPriority priority, boolean receiveCanceled, int sequence) {}

    /** key 为监听方法声明的事件类型；投递时沿被投递事件的继承链逐级查找。 */
    private final Map<Class<?>, List<Handler>> handlers = new ConcurrentHashMap<>();
    private final List<Object> registeredTargets = new CopyOnWriteArrayList<>();

    /** 保证同优先级下「先注册先调用」，且与 handlers 的插入无关。 */
    private int sequenceCounter = 0;

    private static final Comparator<Handler> ORDER =
            Comparator.<Handler>comparingInt(h -> h.priority().getIndex())   // HIGHEST(0) 优先
                    .thenComparingInt(Handler::sequence);                     // 同优先级按注册顺序

    @Override
    public synchronized void register(Object target) {
        Class<?> clazz = target instanceof Class<?> c ? c : target.getClass();
        Object instance = target instanceof Class<?> ? null : target;

        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            Method[] declared = current.getDeclaredMethods();
            // getDeclaredMethods 的顺序未定义，排序以保证确定性
            Arrays.sort(declared, Comparator.comparing(Method::getName)
                    .thenComparing(m -> Arrays.toString(m.getParameterTypes())));

            for (Method method : declared) {
                SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
                if (annotation == null) continue;
                if (!Modifier.isStatic(method.getModifiers()) && instance == null) continue;
                if (method.getParameterCount() != 1) continue;
                Class<?> eventType = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(eventType)) continue;
                method.setAccessible(true);
                this.handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                        .add(new Handler(instance, clazz, method, annotation.priority(), annotation.receiveCanceled(), this.sequenceCounter++));
            }
        }

        this.registeredTargets.add(target);
        this.handlers.values().forEach(list -> list.sort(ORDER));
    }

    @Override
    public synchronized void unregister(Object target) {
        this.registeredTargets.remove(target);
        if (target instanceof Class<?> clazz) {
            // 注销一个订阅者类：只移除该类贡献的静态监听器
            this.handlers.values().forEach(list -> list.removeIf(h -> h.instance() == null && h.owner() == clazz));
        } else {
            this.handlers.values().forEach(list -> list.removeIf(h -> h.instance() == target));
        }
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> listener) {
        // 无法从 lambda 提取泛型事件类型；本兼容层中 mod 未使用 addListener
        throw new UnsupportedOperationException("addListener is not supported in the Fabric compat layer");
    }

    @Override
    public <T extends Event> T post(T event) {
        // 沿继承链收集：Foo.Bar 的投递也要送达监听 Foo 的方法（NeoForge 语义）
        List<Handler> matched = null;
        for (Class<?> type = event.getClass(); type != null && Event.class.isAssignableFrom(type); type = type.getSuperclass()) {
            List<Handler> list = this.handlers.get(type);
            if (list == null || list.isEmpty()) continue;
            if (matched == null) {
                matched = new ArrayList<>(list);
            } else {
                matched.addAll(list);
            }
        }

        if (matched == null) {
            return event;
        }

        if (matched.size() > 1) {
            matched.sort(ORDER);
        }

        for (Handler handler : matched) {
            if (event.isCanceled() && !handler.receiveCanceled()) continue;
            try {
                handler.method().invoke(handler.instance(), event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Throwable cause = e instanceof InvocationTargetException ite ? ite.getCause() : e;
                throw new RuntimeException("Exception dispatching event " + event.getClass().getName()
                        + " to " + handler.method().getDeclaringClass().getName() + "#" + handler.method().getName(), cause);
            }
        }
        return event;
    }
}
