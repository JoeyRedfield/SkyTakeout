package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status,id);

        cleanCache("dish_*");

        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询: {}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除: {}", ids);
        dishService.deleteBatch(ids);

        // 将所有菜品缓存数据清理掉：所有以dish_开头的key
        // 从简操作，不用一个个找
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id获取菜品")
    public Result<DishVO> getById(@PathVariable("id") Long id){
        log.info("根据id查询菜品: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品: {}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 如果只是修改普通的属性（名字，价格）还好，
        // 但是如果修改的是分类，相当于影响2份缓存数据
        // 因此，也是直接删除
        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id获取菜品和口味list")
    public Result<List<DishVO>> list(@RequestParam Long categoryId){
        log.info("根据分类id获取菜品和口味list, 分类id: {}", categoryId);
        List<DishVO> dishVOS = dishService.getByCategoryIdWithFlavor(categoryId);
        return Result.success(dishVOS);
    }

    private void cleanCache(String patten){
        Set keys = redisTemplate.keys(patten);
        redisTemplate.delete(keys);
    }

}
