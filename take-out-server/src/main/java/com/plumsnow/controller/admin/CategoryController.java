package com.plumsnow.controller.admin;

import com.plumsnow.context.BaseContext;
import com.plumsnow.dto.CategoryDTO;
import com.plumsnow.dto.CategoryPageQueryDTO;
import com.plumsnow.entity.Category;
import com.plumsnow.result.PageResult;
import com.plumsnow.result.Result;
import com.plumsnow.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    @CacheEvict(value = "category", allEntries = true)
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}", categoryDTO);

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setShopId(BaseContext.getCurrentShopId());

        categoryService.save(category);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    //@Cacheable(value = "category")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        Long currentShopId = BaseContext.getCurrentShopId();
        categoryPageQueryDTO.setShopId(currentShopId);

        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    @CacheEvict(value = "category", allEntries = true)
    public Result<String> deleteById(Long id){
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    @CacheEvict(value = "category", allEntries = true)
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    @CacheEvict(value = "category", allEntries = true)
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @GetMapping("/select/{id}")
    @ApiOperation("根据id查询分类")
    public Result<Category> selectById(@PathVariable Long id){
        Category category = categoryService.getById(id);

        return Result.success(category);
    }
}