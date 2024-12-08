package top.sephy.infra.mybatis.plus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 批量更新, 只更新非空字段
 * 请确认 JDBC URL rewriteBatchedStatements=true 开启批量模式
 */
public class BatchUpdateById extends AbstractMethod {

    /**
     * @since 3.5.0
     */
    public BatchUpdateById() {
        super("batchUpdateById");
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sqlTemplate =
        """
            "<script>
                <foreach collection=entityList" item="et" separator=";">
                    UPDATE %s %s WHERE %s=#{%s} %s
                </foreach>
            </script>"
        """;
        final String additional = optlockVersion(tableInfo) + tableInfo.getLogicDeleteSql(true, true);
        String sql = String.format(sqlTemplate, tableInfo.getTableName(),
                sqlSet(tableInfo.isWithLogicDelete(), false, tableInfo, false, ENTITY, ENTITY_DOT),
                tableInfo.getKeyColumn(), ENTITY_DOT + tableInfo.getKeyProperty(), additional);
        SqlSource sqlSource = super.createSqlSource(configuration, sql, modelClass);
        return addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
    }
}
