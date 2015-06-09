package jef.pdm;

import java.io.File;
import java.io.IOException;

import jef.common.JefException;
import jef.common.log.LogUtil;

import org.junit.Ignore;
import org.junit.Test;

import com.github.geequery.codegen.pdm.IMetaLoader;
import com.github.geequery.codegen.pdm.PDMetaLoader;
import com.github.geequery.codegen.pdm.model.MetaModel;
import com.github.geequery.codegen.pdm.model.MetaTable;

public class PDMTest {
	private String file = "E:/data_model/数据模型设计-PDM/backup/Customer_Domain.pdm";

	@Test
	@Ignore
	public void main1() throws IOException, JefException {
		IMetaLoader metaLoader = new PDMetaLoader();
		MetaModel model = metaLoader.getMetaModel(new File(file));
		MetaTable table= model.getTable("CM_CUSTOMER");
		System.out.println(table.getName());
		LogUtil.show(table.getImportKeys());
		System.out.println("======================");
		LogUtil.show(table.getExportKeys());
		
		LogUtil.show(model.getReference("Reference_108"));
	}
}
