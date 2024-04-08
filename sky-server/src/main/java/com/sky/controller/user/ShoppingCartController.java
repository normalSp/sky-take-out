package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.BaseContext;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    @Transactional
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加到购物车的信息：{}",shoppingCart);

        //1. 设置用户id，指定是哪个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //2. 查询当前菜品或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        //2.1 判断加入购物车的是菜品还是套餐
        if(null != shoppingCart.getDishId()) {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());

        }else if(null != shoppingCart.getSetmealId()){
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingCartOne = shoppingCartService.getOne(lambdaQueryWrapper);

        if(null != shoppingCartOne) {
            //3. 如果在，则数据+1
            Integer number = shoppingCartOne.getNumber();
            shoppingCartOne.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartOne);
        }else {
            //4. 如果不在，则新增一条数据，数量置为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            //4.1 判断加入购物车的是菜品还是套餐,设置金额
            if(null != shoppingCart.getDishId()) {
                LambdaQueryWrapper<Dish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                lambdaQueryWrapper1.eq(Dish::getId, shoppingCart.getDishId());
                Dish dish = dishService.getOne(lambdaQueryWrapper1);

                BigDecimal price = dish.getPrice();
                Integer number = shoppingCart.getNumber();
                BigDecimal amount = price.multiply(new BigDecimal(number));

                shoppingCart.setAmount(amount);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());

            }else if(null != shoppingCart.getSetmealId()){
                LambdaQueryWrapper<Setmeal> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                lambdaQueryWrapper1.eq(Setmeal::getId, shoppingCart.getSetmealId());
                Setmeal setmeal = setmealService.getOne(lambdaQueryWrapper1);

                BigDecimal price = setmeal.getPrice();
                Integer number = shoppingCart.getNumber();
                BigDecimal amount = price.multiply(new BigDecimal(number));

                shoppingCart.setAmount(amount);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());

            }

            shoppingCartService.save(shoppingCart);
            shoppingCartOne =  shoppingCart;
        }

        return Result.success(shoppingCartOne);
    }

    /**
     * 购物车商品减1
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        if(null != shoppingCart.getDishId()){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }
        else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }


        ShoppingCart shoppingCartOne = shoppingCartService.getOne(lambdaQueryWrapper);

        if(null != shoppingCartOne) {
            Integer number = shoppingCartOne.getNumber();
            if(number > 1) {
                shoppingCartOne.setNumber(number - 1);
                shoppingCartService.updateById(shoppingCartOne);
                return Result.success("减少成功");
            }else {
                shoppingCartService.removeById(shoppingCartOne.getId());
                return Result.success("删除成功");
            }
        }

        return Result.error("更新失败");
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart>  lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        lambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);

        return Result.success(shoppingCarts);
    }


    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(lambdaQueryWrapper);

        return Result.success("清空购物车成功");
    }

}
