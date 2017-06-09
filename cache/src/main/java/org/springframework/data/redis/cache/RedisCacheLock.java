package org.springframework.data.redis.cache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;

/**
 * 基于redis实现的分布式锁
 * 
 * @author majun04
 *
 */
public class RedisCacheLock implements Lock {
    /**
     * StringRedisSerializer
     */
    private static final StringRedisSerializer STRING_SERIALIZER = new StringRedisSerializer();

    private static final Logger LOG = LoggerFactory.getLogger(RedisCacheLock.class);

    private static final String DEFAULT_REDIS_CACHE_LOCK_KEY = "default_redis_cache_lock";

    private static final Lock WRITE_LOCK = new ReentrantLock();
    private static final Lock READ_LOCK = new ReentrantLock();
    /**
     * redisTemplate
     */
    private RedisTemplate<? extends Object, ? extends Object> redisTemplate;
    private byte[] cacheLockBytes = null;

    /**
     * RedisCacheLock
     * 
     * @param redisTemplate redisTemplate
     * @param cacheLockName cacheLockName
     */
    @SuppressWarnings("unchecked")
    private RedisCacheLock(String cacheLockName) {
        super();
        this.redisTemplate =
                (RedisTemplate<? extends Object, ? extends Object>) ApplicationContextHelper.getContext().getBean(
                        "redisTemplate");
        this.cacheLockBytes = STRING_SERIALIZER.serialize(cacheLockName);
    }

    /**
     * RedisCacheLock,如果不指定缓存锁名称， 则采用默认名称
     */
    private RedisCacheLock() {
        this(DEFAULT_REDIS_CACHE_LOCK_KEY);
    }

    /**
     * 返回新生成的RedisCacheLock实例对象
     * 
     * @param redisTemplate redisTemplate
     * @param cacheLockName cacheLockName
     * @return 返回新生成的RedisCacheLock实例对象
     */
    public static RedisCacheLock getInstance(String cacheLockName) {
        RedisCacheLock redisCacheLock = new RedisCacheLock(cacheLockName);
        return redisCacheLock;
    }

    /**
     * 返回新生成的RedisCacheLock实例对象，采用全局的默认lock_key
     * 
     * @return 返回RedisCacheLock锁对象
     */
    public static RedisCacheLock getInstance() {
        RedisCacheLock redisCacheLock = new RedisCacheLock();
        return redisCacheLock;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.locks.Lock#lock()
     */
    @Override
    public void lock() {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.locks.Lock#unlock()
     */
    @Override
    public void unlock() {
        READ_LOCK.tryLock();
        try {
            redisTemplate.execute(new RedisCallback<Boolean>() {
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    Boolean exists = connection.exists(cacheLockBytes);
                    if (exists) {
                        connection.watch(cacheLockBytes);
                        connection.multi();
                        connection.del(cacheLockBytes);
                        connection.exec();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            READ_LOCK.unlock();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.locks.Lock#lockInterruptibly()
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.locks.Lock#tryLock()
     */
    @Override
    public boolean tryLock() {
        boolean finalResult = false;
        WRITE_LOCK.tryLock();
        try {
            // 如果锁存在，直接返回false，如果锁不存在，则直接获取锁
            finalResult = redisTemplate.execute(new RedisCallback<Boolean>() {
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    // 这个需要采用redis自身提供的setNX，将“取”和“放”两步操作作为一个原子操作进行，还能达到在并发情况下，锁行为的一致性
                    Boolean exists = connection.setNX(cacheLockBytes, cacheLockBytes);
                    boolean forReturn = false;
                    if (exists) {
                        connection.watch(cacheLockBytes);
                        connection.multi();
                        connection.set(cacheLockBytes, cacheLockBytes);
                        // 如果上锁之后，需要对锁时间进行调整，避免由于客户端因为一场导致锁不释放，而导致的锁永久存在问题
                        connection.expire(cacheLockBytes, 30);
                        connection.exec();
                        forReturn = true;
                    }
//                    LOG.info(STRING_SERIALIZER.deserialize(cacheLockBytes) + " is exists? " + exists
//                            + " the forReturn is  :" + forReturn);
                    return forReturn;
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            WRITE_LOCK.unlock();
        }

        return finalResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean finalResult = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                boolean retry = true;
                boolean foundLock = false;
                long wait = unit.toMillis(time) + System.currentTimeMillis();
                do {
                    // 如果对应锁存在，并且当前时间还没到重试时间，那么继续重试
                    if (connection.exists(cacheLockBytes) && System.currentTimeMillis() < wait) {
                        retry = true;
                    } else {
                        foundLock = tryLock();
                        retry = false;
                    }
                } while (retry);
                return foundLock;
            }
        });
        return finalResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.locks.Lock#newCondition()
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

}
