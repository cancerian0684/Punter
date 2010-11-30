--<ScriptOptions statementTerminator=";"/>

ALTER TABLE "PUNTER"."TASKHISTORY" DROP CONSTRAINT "TASKHISTORYTASK_ID";

ALTER TABLE "PUNTER"."PROCESSHISTORY" DROP CONSTRAINT "PRCSSHISTORYPRCSSD";

ALTER TABLE "PUNTER"."TASKHISTORY" DROP CONSTRAINT "TSKHSTRPRCSSHSTRYD";

ALTER TABLE "PUNTER"."DOCUMENT_LOB" DROP CONSTRAINT "FK_DOCUMENT_LOB_ID";

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" DROP CONSTRAINT "DCMNTDCMNRFRNCDCSD";

ALTER TABLE "PUNTER"."ATTACHMENT_LOB" DROP CONSTRAINT "ATTACHMENT_LOB_ID";

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" DROP CONSTRAINT "DCMNTDCUMENTDCMNTD";

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" DROP CONSTRAINT "DCMNTDCMNDCSRFRRDD";

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" DROP CONSTRAINT "DCMNTDCMNTRLTDDCSD";

ALTER TABLE "PUNTER"."ATTACHMENT" DROP CONSTRAINT "TTACHMENTDCUMENTID";

ALTER TABLE "PUNTER"."TASK" DROP CONSTRAINT "FK_TASK_PROCESS_ID";

ALTER TABLE "PUNTER"."PROCESSHISTORY" DROP CONSTRAINT "SQL101102231526540";

ALTER TABLE "PUNTER"."PROCESS" DROP CONSTRAINT "SQL101102231526950";

ALTER TABLE "PUNTER"."TASKHISTORY" DROP CONSTRAINT "SQL101102231527480";

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" DROP CONSTRAINT "SQL101102231527080";

ALTER TABLE "PUNTER"."TASK" DROP CONSTRAINT "SQL101102231526730";

ALTER TABLE "PUNTER"."ATTACHMENT" DROP CONSTRAINT "SQL101102231527590";

ALTER TABLE "PUNTER"."DOCUMENT_LOB" DROP CONSTRAINT "SQL101102231527200";

ALTER TABLE "PUNTER"."ATTACHMENT_LOB" DROP CONSTRAINT "SQL101102231527350";

ALTER TABLE "PUNTER"."ID_GEN" DROP CONSTRAINT "SQL101102231528620";

ALTER TABLE "PUNTER"."DOCUMENT" DROP CONSTRAINT "SQL101102231526830";

DROP INDEX "PUNTER"."SQL101102231528160";

DROP INDEX "PUNTER"."SQL101102231527590";

DROP INDEX "PUNTER"."SQL101102231528620";

DROP INDEX "PUNTER"."SQL101102231527930";

DROP INDEX "PUNTER"."SQL101102231527350";

DROP INDEX "PUNTER"."SQL101102231528090";

DROP INDEX "PUNTER"."SQL101102231527080";

DROP INDEX "PUNTER"."SQL101102231526950";

DROP INDEX "PUNTER"."SQL101102231528290";

DROP INDEX "PUNTER"."SQL101102231526730";

DROP INDEX "PUNTER"."SQL101102231527740";

DROP INDEX "PUNTER"."SQL101102231527480";

DROP INDEX "PUNTER"."SQL101102231528370";

DROP INDEX "PUNTER"."SQL101102231528010";

DROP INDEX "PUNTER"."SQL101102231527200";

DROP INDEX "PUNTER"."SQL101102231527840";

DROP INDEX "PUNTER"."SQL101102231526830";

DROP INDEX "PUNTER"."SQL101102231528460";

DROP INDEX "PUNTER"."SQL101102231528270";

DROP INDEX "PUNTER"."SQL101102231526540";

DROP INDEX "PUNTER"."SQL101102231528260";

DROP TABLE "PUNTER"."TASK";

DROP TABLE "PUNTER"."DOCUMENT_DOCUMENT";

DROP TABLE "PUNTER"."DOCUMENT";

DROP TABLE "PUNTER"."ATTACHMENT";

DROP TABLE "PUNTER"."ID_GEN";

DROP TABLE "PUNTER"."PROCESSHISTORY";

DROP TABLE "PUNTER"."DOCUMENT_LOB";

DROP TABLE "PUNTER"."PROCESS";

DROP TABLE "PUNTER"."TASKHISTORY";

DROP TABLE "PUNTER"."ATTACHMENT_LOB";

CREATE TABLE "PUNTER"."TASK" (
		"ID" BIGINT NOT NULL,
		"AUTHOR" VARCHAR(255),
		"OUTPUTPARAMS" BLOB(2147483647),
		"SEQUENCE" INTEGER,
		"DESCRIPTION" VARCHAR(255),
		"INPUTPARAMS" BLOB(2147483647),
		"NAME" VARCHAR(255),
		"ACTIVE" SMALLINT DEFAULT 0,
		"CLASSNAME" VARCHAR(255),
		"OPT_LOCK" BIGINT,
		"PROCESS_ID" BIGINT
	);

CREATE TABLE "PUNTER"."DOCUMENT_DOCUMENT" (
		"DOCSREFERRED_ID" BIGINT NOT NULL,
		"REFERENCEDOCS_ID" BIGINT NOT NULL,
		"DOCUMENT_ID" BIGINT NOT NULL,
		"RELATEDDOCS_ID" BIGINT NOT NULL
	);

CREATE TABLE "PUNTER"."DOCUMENT" (
		"ID" BIGINT NOT NULL,
		"TAG" VARCHAR(255),
		"DATEACCESSED" TIMESTAMP,
		"EXT" VARCHAR(255),
		"OPT_LOCK" BIGINT,
		"DATEUPDATED" TIMESTAMP,
		"AUTHOR" VARCHAR(255),
		"TITLE" VARCHAR(255),
		"CATEGORY" VARCHAR(255),
		"PRIORITY" DOUBLE,
		"ACCESSCOUNT" BIGINT,
		"MD5" VARCHAR(255),
		"DATECREATED" TIMESTAMP,
		"ACTIVE" SMALLINT DEFAULT 0
	);

CREATE TABLE "PUNTER"."ATTACHMENT" (
		"ID" BIGINT NOT NULL,
		"TAG" VARCHAR(255),
		"DATEACCESSED" TIMESTAMP,
		"EXT" VARCHAR(255),
		"OPT_LOCK" BIGINT,
		"AUTHOR" VARCHAR(255),
		"TITLE" VARCHAR(255),
		"CATEGORY" VARCHAR(255),
		"PRIORITY" DOUBLE,
		"ACCESSCOUNT" BIGINT,
		"LENGTH" BIGINT,
		"DATECREATED" TIMESTAMP,
		"ACTIVE" SMALLINT DEFAULT 0,
		"COMMENTS" VARCHAR(255),
		"DOCUMENT_ID" BIGINT
	);

CREATE TABLE "PUNTER"."ID_GEN" (
		"GEN_KEY" VARCHAR(50) NOT NULL,
		"GEN_VALUE" DECIMAL(15 , 0)
	);

CREATE TABLE "PUNTER"."PROCESSHISTORY" (
		"ID" BIGINT NOT NULL,
		"STARTTIME" TIMESTAMP,
		"USERNAME" VARCHAR(255),
		"RUNSTATUS" VARCHAR(255),
		"RUNSTATE" VARCHAR(255),
		"NAME" VARCHAR(255),
		"FINISHTIME" TIMESTAMP,
		"OPT_LOCK" BIGINT,
		"PROCESS_ID" BIGINT
	);

CREATE TABLE "PUNTER"."DOCUMENT_LOB" (
		"ID" BIGINT NOT NULL,
		"CONTENT" BLOB(5242880)
	);

CREATE TABLE "PUNTER"."PROCESS" (
		"ID" BIGINT NOT NULL,
		"USERNAME" VARCHAR(255),
		"DESCRIPTION" VARCHAR(255),
		"INPUTPARAMS" BLOB(2147483647),
		"NAME" VARCHAR(255),
		"COMMENTS" VARCHAR(255),
		"OPT_LOCK" BIGINT
	);

CREATE TABLE "PUNTER"."TASKHISTORY" (
		"ID" BIGINT NOT NULL,
		"RUNSTATUS" VARCHAR(255),
		"LOGS" CLOB(2147483647),
		"STATUS" SMALLINT DEFAULT 0,
		"RUNSTATE" VARCHAR(255),
		"SEQUENCE" INTEGER,
		"OPT_LOCK" BIGINT,
		"PROCESSHISTORY_ID" BIGINT,
		"TASK_ID" BIGINT
	);

CREATE TABLE "PUNTER"."ATTACHMENT_LOB" (
		"ID" BIGINT NOT NULL,
		"CONTENT" BLOB(10485760)
	);

CREATE INDEX "PUNTER"."SQL101102231528160" ON "PUNTER"."DOCUMENT_DOCUMENT" ("DOCUMENT_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231527590" ON "PUNTER"."ATTACHMENT" ("ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231528620" ON "PUNTER"."ID_GEN" ("GEN_KEY" ASC);

CREATE INDEX "PUNTER"."SQL101102231527930" ON "PUNTER"."DOCUMENT_DOCUMENT" ("RELATEDDOCS_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231527350" ON "PUNTER"."ATTACHMENT_LOB" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528090" ON "PUNTER"."DOCUMENT_DOCUMENT" ("DOCSREFERRED_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231527080" ON "PUNTER"."DOCUMENT_DOCUMENT" ("DOCSREFERRED_ID" ASC, "REFERENCEDOCS_ID" ASC, "DOCUMENT_ID" ASC, "RELATEDDOCS_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231526950" ON "PUNTER"."PROCESS" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528290" ON "PUNTER"."TASKHISTORY" ("TASK_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231526730" ON "PUNTER"."TASK" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231527740" ON "PUNTER"."PROCESSHISTORY" ("PROCESS_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231527480" ON "PUNTER"."TASKHISTORY" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528370" ON "PUNTER"."TASKHISTORY" ("PROCESSHISTORY_ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528010" ON "PUNTER"."DOCUMENT_DOCUMENT" ("REFERENCEDOCS_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231527200" ON "PUNTER"."DOCUMENT_LOB" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231527840" ON "PUNTER"."TASK" ("PROCESS_ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231526830" ON "PUNTER"."DOCUMENT" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528460" ON "PUNTER"."ATTACHMENT" ("DOCUMENT_ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528270" ON "PUNTER"."ATTACHMENT_LOB" ("ID" ASC);

CREATE UNIQUE INDEX "PUNTER"."SQL101102231526540" ON "PUNTER"."PROCESSHISTORY" ("ID" ASC);

CREATE INDEX "PUNTER"."SQL101102231528260" ON "PUNTER"."DOCUMENT_LOB" ("ID" ASC);

ALTER TABLE "PUNTER"."PROCESSHISTORY" ADD CONSTRAINT "SQL101102231526540" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."PROCESS" ADD CONSTRAINT "SQL101102231526950" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."TASKHISTORY" ADD CONSTRAINT "SQL101102231527480" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" ADD CONSTRAINT "SQL101102231527080" PRIMARY KEY ("DOCSREFERRED_ID", "REFERENCEDOCS_ID", "DOCUMENT_ID", "RELATEDDOCS_ID");

ALTER TABLE "PUNTER"."TASK" ADD CONSTRAINT "SQL101102231526730" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."ATTACHMENT" ADD CONSTRAINT "SQL101102231527590" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_LOB" ADD CONSTRAINT "SQL101102231527200" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."ATTACHMENT_LOB" ADD CONSTRAINT "SQL101102231527350" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."ID_GEN" ADD CONSTRAINT "SQL101102231528620" PRIMARY KEY ("GEN_KEY");

ALTER TABLE "PUNTER"."DOCUMENT" ADD CONSTRAINT "SQL101102231526830" PRIMARY KEY ("ID");

ALTER TABLE "PUNTER"."TASKHISTORY" ADD CONSTRAINT "TASKHISTORYTASK_ID" FOREIGN KEY ("TASK_ID")
	REFERENCES "PUNTER"."TASK" ("ID");

ALTER TABLE "PUNTER"."PROCESSHISTORY" ADD CONSTRAINT "PRCSSHISTORYPRCSSD" FOREIGN KEY ("PROCESS_ID")
	REFERENCES "PUNTER"."PROCESS" ("ID");

ALTER TABLE "PUNTER"."TASKHISTORY" ADD CONSTRAINT "TSKHSTRPRCSSHSTRYD" FOREIGN KEY ("PROCESSHISTORY_ID")
	REFERENCES "PUNTER"."PROCESSHISTORY" ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_LOB" ADD CONSTRAINT "FK_DOCUMENT_LOB_ID" FOREIGN KEY ("ID")
	REFERENCES "PUNTER"."DOCUMENT" ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" ADD CONSTRAINT "DCMNTDCMNRFRNCDCSD" FOREIGN KEY ("REFERENCEDOCS_ID")
	REFERENCES "PUNTER"."DOCUMENT" ("ID");

ALTER TABLE "PUNTER"."ATTACHMENT_LOB" ADD CONSTRAINT "ATTACHMENT_LOB_ID" FOREIGN KEY ("ID")
	REFERENCES "PUNTER"."ATTACHMENT" ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" ADD CONSTRAINT "DCMNTDCUMENTDCMNTD" FOREIGN KEY ("DOCUMENT_ID")
	REFERENCES "PUNTER"."DOCUMENT" ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" ADD CONSTRAINT "DCMNTDCMNDCSRFRRDD" FOREIGN KEY ("DOCSREFERRED_ID")
	REFERENCES "PUNTER"."DOCUMENT" ("ID");

ALTER TABLE "PUNTER"."DOCUMENT_DOCUMENT" ADD CONSTRAINT "DCMNTDCMNTRLTDDCSD" FOREIGN KEY ("RELATEDDOCS_ID")
	REFERENCES "PUNTER"."DOCUMENT" ("ID");

ALTER TABLE "PUNTER"."ATTACHMENT" ADD CONSTRAINT "TTACHMENTDCUMENTID" FOREIGN KEY ("DOCUMENT_ID")
	REFERENCES "PUNTER"."DOCUMENT" ("ID");

ALTER TABLE "PUNTER"."TASK" ADD CONSTRAINT "FK_TASK_PROCESS_ID" FOREIGN KEY ("PROCESS_ID")
	REFERENCES "PUNTER"."PROCESS" ("ID");

