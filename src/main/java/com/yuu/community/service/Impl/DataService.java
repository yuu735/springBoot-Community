package com.yuu.community.service.Impl;

import com.yuu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    private SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");
    @Autowired
    private RedisTemplate redisTemplate;

    //指定的IP记录到uv
    public void recordUV(String ip){
        String redisKey=RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }
    //查询指定日期范围内的uv
    public long calculateUV(Date start,Date end){

        if(start==null || end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(start.after(end)){
            //start在end后面
            System.out.println("计算uv出错！请输入正确的开始时间段!=======================");
            throw new IllegalArgumentException("请输入正确的开始时间段!");
        }
        //需要先将这范围内的日期做成一组key才能变成数组
        //得到这范围内每天的key是多少：所以需要从开始日期遍历到结束日期
        List<String> keyList=new ArrayList<>();
        //为了能对日期做运算（遍历的时候日期需要加一），用calendar
        Calendar calendar=Calendar.getInstance();   //实例化
        calendar.setTime(start);    //设置开始日期，用calendar做循环
        //当这个时间是小于等于end的时候执行循环内容
        while(!calendar.getTime().after(end)){  //当前日期.after(end)=表示当前日期晚于end，加上！表示不晚于end
            String key=RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //合并数据
        String redisKey=RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());
        //返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //指定用户记录到dau
    public void recordDAU(int userId){
        String redisKey=RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }
    //查询指定日期范围内的dau
    public long calculateDAU(Date start,Date end){
        if(start==null || end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(start.after(end)){
            //start在end后面
            System.out.println("计算dau出错！请输入正确的开始时间段!=======================");
            throw new IllegalArgumentException("请输入正确的开始时间段!");
        }
        //整理该日期范围内的key：从开始日期遍历到结束日期
        //bitmap运算需要的类型是byte数组，因此这里用byte[]来存
        List<byte[]> keyList=new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key=RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            //当这个时间是小于等于end的时候
            calendar.add(Calendar.DATE,1);  //日期加一往下走d
        }
        //进行or运算:期间有登录就算有所以用or
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey=RedisKeyUtil.getDAUKey(df.format(start),df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }


}
