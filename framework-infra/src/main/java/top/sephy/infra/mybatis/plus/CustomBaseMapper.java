/*
 * Copyright 2022-2025 sephy.top
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.sephy.infra.mybatis.plus;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;

import lombok.NonNull;
import top.sephy.infra.entity.DeleteLog;
import top.sephy.infra.mybatis.MyBatisConstants;
import top.sephy.infra.mybatis.plus.intercepter.DeleteLogInterceptor;

/**
 * @author sephy
 * @date 2020-03-07 23:33
 */
public interface CustomBaseMapper<T> extends BaseMapper<T> {

    int upsert(T entity);

    /**
     * 全字段插入
     * 
     * @param entity
     * @return
     */
    int insertAllColumns(T entity);

    /**
     * 多个插入, insert values 模式, 忽略自增主键
     * 
     * @param entityList
     * @return
     */
    int insertBatchSomeColumn(Collection<T> entityList);

    /**
     * 多个插入, insert values 模式, 会插入所有字段(包含主键字段)
     *
     * @param entityList
     * @return
     */
    @Deprecated
    default int insertAllColumnList(@Param("entityList") Collection<T> entityList) {
        return this.batchInsertAllColumn(entityList);
    }

    int batchInsertAllColumn(@Param("entityList") Collection<T> entityList);

    int alwaysUpdateSomeColumnById(@Param(Constants.ENTITY) T entity);

    int batchUpdateById(@Param("entityList") Collection<T> entityList);

    /**
     * 查询全表
     * 
     * @return
     */
    default List<T> selectAll() {
        return PageHelper.offsetPage(0, 10000).doSelectPage(() -> this.selectList(Wrappers.emptyWrapper()));
    }

    /**
     *
     * @param deleteIds
     * @param createdBy
     * @param updatedBy
     * @return
     * @deprecated use {@link DeleteLogInterceptor} instead
     */
    @Deprecated
    int insertDeleteLogByIds(@Param("deleteIds") Collection<Long> deleteIds,
            @Param(value = MyBatisConstants.PARAM_CREATED_BY) Long createdBy,
            @Param(MyBatisConstants.PARAM_UPDATED_BY) Long updatedBy);

    /**
     *
     * @param queryWrapper
     * @param createdBy
     * @param updatedBy
     * @return
     * @deprecated use {@link DeleteLogInterceptor} instead
     */
    @Deprecated
    int insertDeleteLogByQueryWrapper(@Param("ew") Wrapper<T> queryWrapper,
            @Param(value = MyBatisConstants.PARAM_CREATED_BY) Long createdBy,
            @Param(MyBatisConstants.PARAM_UPDATED_BY) Long updatedBy);

    /**
     * 物理删除前先插入删除日志
     * 
     * @param id
     * @return
     * @deprecated use {@link DeleteLogInterceptor} instead
     */
    @Deprecated
    default int deleteAndSaveDeleteLogById(@NonNull Long id) {
        this.insertDeleteLogByIds(List.of(id), null, null);
        return this.deleteById(id);
    }

    /**
     * 物理删除前先插入删除日志
     *
     * @param ids
     * @return
     * @deprecated use {@link DeleteLogInterceptor} instead
     */
    @Deprecated
    default int deleteAndSaveDeleteLogByIds(@NonNull Collection<Long> ids) {
        DeleteLog deleteLog = new DeleteLog();
        deleteLog.setDeleteIds(ids);
        this.insertDeleteLogByIds(ids, null, null);
        return this.deleteBatchIds(ids);
    }

    /**
     * 物理删除前先插入删除日志
     *
     * @param ids
     * @return
     * @deprecated use {@link DeleteLogInterceptor} instead
     */
    @Deprecated
    default int deleteAndSaveDeleteLogByQueryWrapper(@NonNull Wrapper<T> queryWrapper) {
        this.insertDeleteLogByQueryWrapper(queryWrapper, null, null);
        return this.delete(queryWrapper);
    }
}
