package com.spauter.extra.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.spauter.extra.baseentity.utils.ValueUtil.getIntValue;
import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

@Slf4j
@Service
public class RedisCommand {
    @Resource
    @Qualifier("customRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    public void redisCommand(String input) {
        input = input.toLowerCase();
        if (input.startsWith("set")) {
            set(input);
        } else if (input.startsWith("get")) {
            get(input);
        }else if (input.startsWith("remove")) {
            remove(input);
        }
    }


    /**
     * 处理Redis的SET命令。支持两种格式：
     * <p><tr>set key value</tr></p>
     * <p><tr>set key value timeout(秒)</tr></p>
     * 需要输入前缀{@code redis:}
     */
    private void set(String input) {
        String[] strings = input.split(" ");
        if (strings.length < 2 || strings.length > 4) {
            log.error("""
                    \n
                    Error setting command {},
                    You can use the following command:
                    redis:set key value
                    redis:set key value timeout(second)
                    """, input);
            return;
        }
        String key = strings[1];
        String value = strings[2];
        int timeout = 0;
        if (strings.length == 4) {
            timeout = getIntValue(strings[3]);
            if (timeout <= 0) {
                log.error("Illegal timeout value:{}", strings[3]);
                return;
            }
        }
        try {
            if (timeout == 0) {
                redisTemplate.opsForValue().set(key, value);
            } else {
                redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            }
            log.info("{} set to {}", key, value);
        } catch (Exception e) {
            log.error("set value fail because{}", e.getMessage());
        }
    }

    /**
     * 处理Redis的GET命令.
     * 支持两种格式：
     * <p>1. get key - 获取指定key的值</p>
     * <p>2. get * - 获取所有key的值</p>
     * @param input 用户输入的GET命令字符串，格式应为"get key"或"get *"
     */
    private void get(String input) {
        String[] strings = input.split(" ");
        if (strings.length != 2) {
            log.error("""
                    \n
                    Error setting command {},
                    You can use the following command:
                    redis:get key
                    redis:get * (will get all key)
                    """, input);
            return;
        }
        String key = strings[1];
        if (key.equals("*")) {
            getAll();
        } else {
            try {
                Object value = redisTemplate.opsForValue().get(key);
               if(value==null){
                   log.warn("{} is empty",key);
               }else {
                   log.info("{} get value: {}", key, value);
               }
            } catch (Exception e) {
                log.error("get value fail because{}", e.getMessage());
            }
        }
    }

    /**
     * 获取Redis中所有的key-value对
     * 会先使用keys("*")获取所有key，然后逐个查询对应的value
     * <p>注意：keys("*")操作在生产环境大数据量时可能会有性能问题</p>
     */
    private void getAll() {
        log.warn("WARNING: This will cause a lot of network traffic when the Redis database is large!Are you sure?(y or n)");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input == null || !input.trim().equalsIgnoreCase("y")) {
            return;
        }
        log.info("staring get all keys");
        Set<String> keys = redisTemplate.keys("*");
        if(isBlank(keys)){
            log.warn("Keys is empty");
        }
        for (String key : keys) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                System.out.println(key+":"+value);
            } catch (Exception e) {
                log.warn("get value fail because{}", e.getMessage());
            }
        }
    }

    /**
     * 处理Redis的REMOVE命令，用于删除指定的key或所有key
     * 支持两种格式：
     * <p>1. remove key - 删除指定key</p>
     * <p>2. remove * - 删除所有key（谨慎使用）</p>
     *
     */
    private void remove(String input){
        String[] strings = input.split(" ");
        if (strings.length != 2) {
            log.error("""
                    \n
                    Error setting command {},
                    You can use the following command:
                    redis:remove key
                    redis:remove * (will remove all key)
                    """, input);
            return;
        }
        String key = strings[1];
        if (key.equals("*")) {
            log.warn("WARNING: This will delete all data in current Redis database!Are you sure?(y or n)");
            Scanner scanner = new Scanner(System.in);
            String input2 = scanner.nextLine();
            if (input2 == null || !input2.trim().equalsIgnoreCase("y")) {
                return;
            }
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
            log.info("Redis database flushed successfully");
        } else {
            try {
                redisTemplate.delete(key);
                log.info("{} remove success", key);
            } catch (Exception e) {
                log.error("remove value fail because{}", e.getMessage());
            }
        }
    }
}
