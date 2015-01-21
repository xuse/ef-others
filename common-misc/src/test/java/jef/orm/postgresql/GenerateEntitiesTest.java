package jef.orm.postgresql;

import java.io.File;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jef.codegen.EntityGenerator;
import jef.codegen.MetaProvider.DbClientProvider;
import jef.codegen.ast.JapaParser;
import jef.codegen.ast.JavaField;
import jef.codegen.ast.JavaUnit;
import jef.database.dialect.ColumnType;
import jef.database.dialect.AbstractDialect;
import jef.database.meta.Column;
import jef.tools.StringUtils;
import jef.tools.collection.CollectionUtil;
import junit.framework.TestSuite;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 从PostgreSQL数据库生成entity测试类
 * 
 * @see EntityGenerator
 * 
 * @Company Asiainfo-Linkage Technologies (China), Inc.
 * @author luolp@asiainfo-linkage.com
 * @Date 2012-7-25
 */
@Ignore
public class GenerateEntitiesTest extends PostgreSQLTestBase {

	private EntityGenerator generator;
	private JapaParser japaParser = new JapaParser();
	private Column columnDesc;

	private List<String> notNullFields = new ArrayList<String>();
	private List<String> numberFields = new ArrayList<String>();
	private List<String> varcharFields = new ArrayList<String>();
	private List<String> charFields = new ArrayList<String>();
	private List<String> blobFields = new ArrayList<String>();
	private List<String> clobFields = new ArrayList<String>();
	private List<String> dateFields = new ArrayList<String>();
	private List<String> timeStampFields = new ArrayList<String>();
	private List<String> booleanFields = new ArrayList<String>();

	@Before
	public void setUp() {
		generator = new EntityGenerator();
		generator.setProfile(AbstractDialect.getProfile("postgresql"));
		generator.setProvider(new DbClientProvider(db));
		generator.setSrcFolder(new File("src1"));
		generator.setBasePackage("jef.generated.dataobject.postgresql");
	}

	/**
	 * 测试由{@code TestSuite}过程中产生的表生成entity（即 DB->ENTITY过程）是否成功。
	 * 
	 * @see TestSuite
	 */
	@Test
	public void testGenerateEntities() {
		try {
			// 列类型为数组类型的表，过滤掉；
			// 若实际数据库环境中存在其他含有列类型为数组、自定义类型的，也需过滤掉。
			generator.addExcludePatter("arr");
			// PostgreSQL 8.2有以下几张表，过滤掉
			generator.addExcludePatter("pg_ts_cfg");
			generator.addExcludePatter("pg_ts_cfgmap");
			generator.addExcludePatter("pg_ts_dict");
			generator.addExcludePatter("pg_ts_parser");
			generator.setMaxTables(999);
			generator.generateSchema();
		} catch (SQLException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * 测试PostgreSQL的各种列类型到{@code ColumnType}类型的映射关系。
	 * 
	 * @see ColumnType
	 */
	@Test
	public void testGenerateOne() {
		try {
			prepareDbByNativeSqls(isLE82version ? "postgresql_createtable_82_test.sql"
					: "postgresql_createtable_test.sql");

			File file = generator.generateOne("test_columntypes_db2entity",
					"TestColumntypesDb2entity", "");
			JavaUnit javaUnit = japaParser.parse(file, "UTF-8");
			System.out.println(javaUnit.toString());

			prepareNotNullField();
			prepareNumberField();
			prepareVarcharField();
			prepareCharField();
			prepareBlobField();
			prepareClobField();
			prepareDateField();
			prepareTimeStampField();
			prepareBooleanField();

			for (String fieldName : javaUnit.getFieldNames()) {
				handleField(javaUnit, fieldName);
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private void prepareNotNullField() {
		notNullFields.add("smallintfield");
		notNullFields.add("floatfield");
		notNullFields.add("doublefield");
		notNullFields.add("serialfield");
		notNullFields.add("serialfield2");
		notNullFields.add("booleanfield1");
	}

	private void prepareNumberField() {
		numberFields.add("smallintfield");
		numberFields.add("int2field");
		numberFields.add("intfield");
		numberFields.add("bigintfield");
		numberFields.add("decimalfield");
		numberFields.add("numericfield");
		numberFields.add("numericfield2");
		numberFields.add("realfield");
		numberFields.add("floatfield");
		numberFields.add("doublefield");
		numberFields.add("doublefield2");
		numberFields.add("serialfield");
		numberFields.add("serialfield2");
	}

	private void prepareVarcharField() {
		varcharFields.add("moneyfield");
		varcharFields.add("varcharfield1");
		varcharFields.add("varcharfield2");
		varcharFields.add("varbitfield1");
		varcharFields.add("varbitfield2");
		varcharFields.add("cidrfield");
		varcharFields.add("inetfield");
		varcharFields.add("macaddrfield");
		varcharFields.add("uuidfield");
		varcharFields.add("tsvectorfield");
		varcharFields.add("tsqueryfield");
		varcharFields.add("txidfield");
		varcharFields.add("boxfield");
		varcharFields.add("circlefield");
		varcharFields.add("linefield");
		varcharFields.add("lsegfield");
		varcharFields.add("pathfield");
		varcharFields.add("pointfield");
		varcharFields.add("polygonfield");
		varcharFields.add("intervalfield");
	}

	private void prepareCharField() {
		charFields.add("charfield1");
		charFields.add("charfield2");
		charFields.add("bitfield1");
		charFields.add("bitfield2");
	}

	private void prepareBlobField() {
		blobFields.add("binaryfield");
	}

	private void prepareClobField() {
		clobFields.add("textfield");
		clobFields.add("xmlfield");
	}

	private void prepareDateField() {
		dateFields.add("datefield");
	}

	private void prepareTimeStampField() {
		timeStampFields.add("timestampfield1");
		timeStampFields.add("timestampfield2");
		timeStampFields.add("timefield1");
		timeStampFields.add("timefield2");
	}

	private void prepareBooleanField() {
		booleanFields.add("booleanfield1");
		booleanFields.add("booleanfield2");

	}

	private void handleField(JavaUnit javaUnit, String fieldName) {
		JavaField field = javaUnit.getFieldAsJavaField(fieldName);
		for (String fieldAnno : field.getAnnotation()) {
			if (fieldAnno.startsWith("@Column")) {
				Map<String, String> colProps = splitAnnotationToMap(fieldAnno);
				if (!CollectionUtil.isEmpty(colProps)) {
					columnDesc = new Column();
					for (Map.Entry<String, String> item : colProps.entrySet()) {
						buildColumnDesc(colProps, item, columnDesc);
					}

					assertField(field, fieldName);
				}
			}
			System.out.printf("fieldName:%s, fieldAnno:%s\r\n", fieldName,
					fieldAnno);
		}
	}

	private void assertField(JavaField field, String fieldName) {
		Assert.assertEquals(fieldName, columnDesc.getColumnName());
		if (notNullFields.contains(fieldName)) {
			Assert.assertFalse(columnDesc.isNullAble());
		}
		if (numberFields.contains(fieldName)) {
			Assert.assertEquals(Types.INTEGER, columnDesc.getDataTypeCode());
		}
		if (varcharFields.contains(fieldName)) {
			Assert.assertEquals(Types.VARCHAR, columnDesc.getDataTypeCode());
		}
		if (charFields.contains(fieldName)) {
			Assert.assertEquals(Types.CHAR, columnDesc.getDataTypeCode());
		}
		if (blobFields.contains(fieldName)) {
			Assert.assertEquals(Types.BLOB, columnDesc.getDataTypeCode());
		}
		if (clobFields.contains(fieldName)) {
			Assert.assertEquals(Types.CLOB, columnDesc.getDataTypeCode());
		}
		if (dateFields.contains(fieldName)) {
			Assert.assertEquals(Types.DATE, columnDesc.getDataTypeCode());
		}
		if (timeStampFields.contains(fieldName)) {
			Assert.assertEquals(Types.TIMESTAMP, columnDesc.getDataTypeCode());
		}
		if (booleanFields.contains(fieldName)) {
			Assert.assertEquals(Types.BOOLEAN, columnDesc.getDataTypeCode());
		}
		if ("numericfield2".equals(fieldName)) {
			Assert.assertEquals(5, columnDesc.getColumnSize());
		}
		if ("floatfield".equals(fieldName)) {
			Assert.assertEquals("Float", field.getType().getName());
		}
		if ("charfield2".equals(fieldName) || "bitfield2".equals(fieldName)) {
			Assert.assertEquals(1, columnDesc.getColumnSize());
		}
		if ("serialfield".equals(fieldName)) {
			Assert.assertTrue(ArrayUtils.contains(field.getAnnotation(), "@Id"));
		}
	}

	private Map<String, String> splitAnnotationToMap(String annotation) {
		return StringUtils.toMap(
				StringUtils.substringsBetween(annotation, "(", ")")[0], ",",
				"=",0);
	}

	private void buildColumnDesc(Map<String, String> colProps,
			Entry<String, String> item, Column columnDesc) {
		String key = item.getKey();
		String value = item.getValue();
		if (value.contains("\"")) {
			value = StringUtils
					.substringsBetween(colProps.get(key), "\"", "\"")[0];
		}

		if ("name".equals(key)) {
			columnDesc.setColumnName(value);
		} else if ("columnDefinition".equals(key)) {
			columnDesc.setDataType(value);
		} else if ("length".equals(key) || "precision".equals(key)) {
			columnDesc.setColumnSize(Integer.parseInt(value));
		} else if ("nullable".equals(key)) {
			columnDesc.setNullAble(Boolean.parseBoolean(value));
		}
	}

}
