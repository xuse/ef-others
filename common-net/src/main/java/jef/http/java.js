var mNote = false;
var reDiv = /\W/;
var reNum = /\d/;
var reChar = /[a-zA-Z_]/;

function readJavaSource() {
	//根据设定的name来读取textarea的源代码
	var oTextarea = document.getElementsByName("java");
	for(var i = 0; i < oTextarea.length; i++) {
		//遍历所有出现的textarea源代码组并将每篇按换行分开存入数组
		var sourceCode = oTextarea[i].value;
		var codeArray = sourceCode.split("\n");
		var lineCounter = 1;
		//创建文档碎片以及ol节点
		var oFragment = document.createDocumentFragment();
		var oOl = document.createElement("ol");
		//根据源码数组长度的字符串的长度来设定ol的左补丁值
		var marginLeft = (codeArray.length.toString().length-1) * 9 + 30;
		oOl.style.margin = "0px 0px 0px " + marginLeft + "px";
		for(var j = 0; j < codeArray.length; j++) {
			//遍历源代码数组，每次创建li节点并利用innerHTML特性将分析源码
			var oLi = document.createElement("li");
			var oDiv = document.createElement("div");
			oDiv.innerHTML = analyseJavaLine(codeArray[j]);
			//隔行设置背景色
			if(lineCounter == 1) oLi.className = "alt";
			lineCounter *= -1;
			oLi.appendChild(oDiv);
			oOl.appendChild(oLi);
		}
		//将所有创建好的节点附加到文档碎片上，再替换原先的textarea
		oFragment.appendChild(oOl);
		oTextarea[i].parentNode.appendChild(oFragment);
		oTextarea[i].className = "hide";
	}
}
function analyseJavaLine(sString) {
	if(sString == "" || sString == " ") return "&nbsp;"; //当此行为空时直接返回一个空格
	var sArray = new Array();
	var i = 0;
	//如果多行注释模式状态为打开，则继续寻找结尾注释
	if(mNote == true) {
		var j = 0;
		var tString = new String();
		var tArray = new Array();
		for(; j < sString.length-1; j++) {
			if(sString.charAt(j) == '*' && sString.charAt(j+1) == '/') { //如果找到了
				tString = sString.slice(0, j+2);
				tArray = tString.split("");
				for(var k = 0; k < j+2; k++) {
					tArray[k] = transferred(tArray[k]);
				}
				sArray.push("<span class=\"note\">" + tArray.join("") + "</span>");
				mNote = false;
				i = j+2;
				break;
			}
		}
		if(j >= sString.length-1) { //如果抵达行末尾直接返回
			mNote = true;
			tString = sString.slice(i);
			tArray = tString.split("");
			for(var k = 0; k < tArray.length; k++) {
				tArray[k] = transferred(tArray[k]); //转义注释中的特殊字符
			}
			sArray.push("<span class=\"note\">" + tArray.join("") + "</span>");
			return sArray.join("");
		}
	}
	//先从行首开始分析以免首字符不是分隔符时被漏掉
	if(reDiv.test(sString.charAt(i)) == false && mNote == false) {
		for(var j = i+1; j < sString.length; j++) {
			//查找首次出现的分隔符并将之前的字符串装入数组
			if(reDiv.test(sString.charAt(j)) == true) {
				sArray.push(isJavaKey(sString.slice(i, j)));
				i = j;
				break;
			}
		}
	}
	//分析每一行源码，逐字查找分隔符
	outer:
	for(; i < sString.length; i++) {
		if(reDiv.test(sString.charAt(i)) == true) { //如果它是个分隔符
			if(sString.charAt(i) == '/' && sString.charAt(i+1) == '/') { //如果出现//单行注释符
				var tm = 1; //标识之前出现的转义字符次数
				for(var j = i-1; j >= 0; j--) {
					if(sString.charAt(j) != '\\') break;
					tm *= -1;
				}
				if(tm == 1) { //并且这个注释符没有被转义，直接将此行后面所有的东西作为注释装入数组并跳出循环
					var tString = sString.slice(i);
					var tArray = tString.split("");
					for(var k = 0; k < tArray.length; k++) {
						tArray[k] = transferred(tArray[k]); //转义注释中的特殊字符
					}
					sArray.push("<span class=\"note\">" + tArray.join("") + "</span>");
					break outer;
				}
			}
			else if(sString.charAt(i) == '/' && sString.charAt(i+1) == '*') { //如果出现/*多行注释符并且未被转义
				var tm = 1;
				for(var j = i-1; j >= 0; j--) {
					if(sString.charAt(j) != '\\') break;
					tm *= -1;
				}
				if(tm == 1) {
					mNote = true; //标记多行注释模式打开
					var j = i+2;
					var tString = new String();
					var tArray = new Array();
					for(; j < sString.length-1; j++) {
						//查找多行注释符结束标签*/
						if(sString.charAt(j) == '*' && sString.charAt(j+1) == '/') {
							tString = sString.slice(i, j+2);
							tArray = tString.split("");
							for(var k = 0; k < tArray.length; k++) {
								tArray[k] = transferred(tArray[k]); //转义注释中的特殊字符
							}
							sArray.push("<span class=\"note\">" + tArray.join("") + "</span>");
							mNote = false; //找到后将多行注释模式关闭
							i = j+1;
							continue outer;
						}
					}
					if(j >= sString.length-1) { //如果已抵达行末尾说明未找到
						mNote = true;
						tString = sString.slice(i);
						tArray = tString.split("");
						for(var k = 0; k < tArray.length; k++) {
							tArray[k] = transferred(tArray[k]); //转义注释中的特殊字符
						}
						sArray.push("<span class=\"note\">" + tArray.join("") + "</span>");
						break outer;
					}
				}
			}
			else if(sString.charAt(i) == '"') { //如果它是双引号
				var tm_1 = 1;
				for(var j = i-1; j >= 0; j--) {
					if(sString.charAt(j) != '\\') break;
					tm_1 *= -1;
				}
				if(tm_1 == 1) { //并且未被转义
					for(var j = i+1; j < sString.length; j++) {
						//查找下一个未被转义的双引号并将两者之间的内容作为字符串着色装入数组
						if(sString.charAt(j) == '\"') {
							var tm_2 = 1;
							for(var k = j-1; k >i; k--) {
								if(sString.charAt(k) != '\\') break;
								tm_2 *= -1;
							}
							if(tm_2 == 1) {
								var tString = sString.slice(i, j+1);
								var tArray = tString.split("");
								for(var k = 0; k < tArray.length; k++) {
									tArray[k] = transferred(tArray[k]); //转义字符串中的特殊字符
								}
								sArray.push("<span class=\"string\">" + tArray.join("") + "</span>");
								i = j;
								continue outer;
							}
						}
					}
				}
				else { //或者是个转义字符
					sArray.push("\"");
					continue outer;
				}
			}
			else if(sString.charAt(i) == '\'') { //如果是单引号
				var tm =1;
				for(var j = i-1; j >= 0; j--) {
					if(sString.charAt(j) != '\\') break;
					tm *= -1;
				}
				if(tm == 1) { //且没有被转义
					if(sString.charAt(i+2) == '\'' && sString.charAt(i+1) != '\\') { //如果和下一个单引号之间夹着的是一个字符则着色
						sArray.push("<span class=\"char\">'" + sString.charAt(i+1) + "'</span>");
						i += 2;
						continue outer;
					}
					else if(sString.charAt(i+3) == '\'' && sString.charAt(i+1) == '\\') { //或者是转义字符也着色
						sArray.push("<span class=\"char\">'" + sString.slice(i+1, i+3) + "'</span>");
						i += 3;
						continue outer;
					}
				}
				else { //如果被转义了
					sArray.push("'");
					continue outer;
				}
			}
			//如果此字符是其它分隔符，则从当前位置查找下一分隔符
			else {
				if(sString.charAt(i) == '-') { //如果是减号还要判断它是否作为符号出现
					var j = i-1, k = i+1;
					for(; j >= 0; j--) { //解析减号之前的非空符是否是分隔符，如是是并且不是减号时而且之后也不是减号时它则为负号着色
						if(sString.charAt(j) != ' ' && sString.charAt(j) != '\t') break;
					}
					if(reDiv.test(sString.charAt(j)) == true && sString.charAt(j) != '-' && sString.charAt(i+1) != '-') sArray.push("<span class=\"num\">-</span>");
					else sArray.push("-");
				}
				else sArray.push(transferred(sString.charAt(i)));
				for(var j = i+1; j < sString.length; j++) {
					if(reDiv.test(sString.charAt(j)) == true) {
						if(j > i+1) { //防止分隔符连续出现把空值装入数组
							sArray.push(isJavaKey(sString.slice(i+1, j))); //如果是关键字或数字则着色，否则直接填入数组
						}
						i = j-1;
						continue outer;
					}
				}
			}
		}
	}
	return sArray.join("");
}
function isJavaKey(sKey) {
	for(var i = 0; i < javaKeyWords.length; i++) {
		if(sKey == javaKeyWords[i]) return "<span class=\"javakeywords\">" + sKey + "</span>";
	}
	return isNum(sKey);
}
function isNum(sNum) {
	if(reNum.test(sNum) == true && reChar.test(sNum) == false) return "<span class=\"num\">" + sNum + "</span>";
	else return sNum;
}
function transferred(tDiv) {
	if(tDiv == '\t') return "&nbsp;&nbsp;&nbsp;&nbsp;";
	else if(tDiv == '&') return "&amp;";
	else if(tDiv == '<') return "&lt;";
	else if(tDiv == '>') return "&gt;";
	else if(tDiv == '\"') return "&quot;";
	else if(tDiv == ' ') return "&nbsp;";
	else return tDiv;
}

//关键字集合
var javaKeyWords = ("abstract assert boolean break byte case catch char class const " +
				"continue default do double else enum extends " +
				"false final finally float for goto if implements import " +
				"instanceof int interface long native new null " +
				"package private protected public return " +
				"short static strictfp super switch synchronized this throw throws true " +
				"transient try void volatile while").split(" ");
readJavaSource();