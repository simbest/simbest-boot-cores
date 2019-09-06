/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.distributed.lock;

import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 用途：Redisson分布式锁和同步器
 * 参考：https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
 * 作者: lishuyi
 * 时间: 2018/6/22  17:40
 */
@Slf4j
@Component
public class DistributedRedisLock {

    public final static String REDISSON_LOCK = "redisson_lock_";

    public final static String TASK_SCHEDULE_LOCK = "TASK_SCHEDULE_LOCK:";
    
    @Autowired
    private RedissonClient redisson;

    @Autowired
    private AppConfig config;

    private static String redisKeyPrefix;

    private static DistributedRedisLock lockUtils;

    @PostConstruct
    public void init() {
        lockUtils = this;
        lockUtils.redisson = this.redisson;
        lockUtils.redisKeyPrefix = config.getRedisKeyPrefix() + TASK_SCHEDULE_LOCK;
    }

    /**
     * 强制获取锁
     * @param lockName
     */
    public static void lock(String lockName){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        mylock.lock();
    }

    /**
     * 强制获取锁
     * @param lockName
     */
    public static void lock(String lockName, int seconds){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        //lock提供带timeout参数，timeout结束强制解锁，防止死锁
        mylock.lock(seconds, ApplicationConstants.REDIS_LOCK_DEFAULT_TIME_UNIT);
    }

    /**
     * 强制释放锁
     * @param lockName
     */
    public static void unlock(String lockName){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        mylock.unlock();
    }


    /**
     * 尝试获得锁
     * @param lockName
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> T tryLock(String lockName, DistributedLockCallback<T> callback){
        return tryLock(lockName, ApplicationConstants.REDIS_LOCK_WAIT_TIMEOUT, ApplicationConstants.REDIS_LOCK_RELEASE_TIMEOUT, callback);
    }

    /**
     * 尝试获得锁后执行回调
     * @param lockName
     * @param waitSeconds
     * @param releaseSeconds
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> T tryLock(String lockName, long waitSeconds, long releaseSeconds, DistributedLockCallback<T> callback){
        T returnObj = null;
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        try {
            //最多等待waitSeconds秒，获得锁后releaseSeconds秒自动解锁
            boolean locked = mylock.tryLock(waitSeconds, releaseSeconds, ApplicationConstants.REDIS_LOCK_DEFAULT_TIME_UNIT);
            if(locked){
                returnObj = callback.process();
            }
        }
        catch (RedissonShutdownException e) {
            log.warn("尝试加锁失败，发生【{}】异常", e.getMessage());
        }
        catch (InterruptedException e) {
            log.warn("尝试加锁失败，发生【{}】异常", e.getMessage());
        } finally {
            mylock.unlock();
        }
        return returnObj;
    }

    /**
     * 仅尝试获得锁， 需要获得锁的业务手动释放锁
     * @param lockName
     * @param waitSeconds
     * @param releaseSeconds
     * @return
     */
    public static DistributedRedissonLock tryLock(String lockName, long waitSeconds, long releaseSeconds){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        try {
            //最多等待waitSeconds秒，获得锁后releaseSeconds秒自动解锁
            boolean locked = mylock.tryLock(waitSeconds, releaseSeconds, ApplicationConstants.REDIS_LOCK_DEFAULT_TIME_UNIT);
            return DistributedRedissonLock.builder().rLock(mylock).isLocked(locked).build();
        }
        catch (RedissonShutdownException e) {
            log.warn("尝试加锁失败，发生【{}】异常", e.getMessage());
        }
        catch (InterruptedException e) {
            log.warn("尝试加锁失败，发生【{}】异常", e.getMessage());
        }
        return DistributedRedissonLock.builder().rLock(mylock).isLocked(false).build();
    }

}
