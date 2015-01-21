--
-- MySQL
drop table if exists JFILE;
create table JFILE (
	UUID  varchar(42),
	REALNAME varchar(255),
	CREATETIME  timestamp,
	DESCRIPTION  varchar(255),
	STORAGEFOLDER varchar(255),
	APPLICATIONCODE varchar(60),
	STORAGETYPE varchar(20),
	primary key (UUID)
);

---------------------------------
--Derby
---------------------------
drop table JFILE;
create table JFILE (
	UUID  varchar(42),
	REALNAME varchar(255),
	CREATETIME  timestamp,
	DESCRIPTION  varchar(255),
	STORAGEFOLDER varchar(255),
	APPLICATIONCODE varchar(60),
	STORAGETYPE varchar(20),
	primary key (UUID)
);

----------------------------------------
--Oracle 
---------------------------------------
drop table JFILE;
create table JFILE(
	UUID varchar2(42) not null,
	REALNAME varchar2(255) not null,
	CREATETIME timestamp not null,
	DESCRIPTION varchar2(255),
	STORAGEFOLDER varchar2(255) not null,
	APPLICATIONCODE varchar2(60),
	STORAGETYPE varchar2(20) not null,
	constraint PK_JFILE primary key(UUID)
)