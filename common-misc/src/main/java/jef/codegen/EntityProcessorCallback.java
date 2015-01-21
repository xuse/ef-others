package jef.codegen;

import jef.codegen.ast.JavaField;
import jef.codegen.ast.JavaUnit;
import jef.database.dialect.ColumnType;
import jef.database.meta.Column;


public interface EntityProcessorCallback{
	void setTotal(int n);
	void init(Metadata meta, String tablename, String tableComment, String schema,JavaUnit java);
	void addField(JavaUnit java, JavaField field, Column c, ColumnType columnType);
	void finish(JavaUnit java);
}
