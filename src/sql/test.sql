CREATE OR REPLACE PROCEDURE myproc
IS

stringa varchar2 (200);
cursor cur is select * from user_objects order by object_type desc;

begin
for c in cur loop
stringa := '';

if c.object_type = 'VIEW' then
stringa := 'drop view ' || c.object_name;
EXECUTE immediate stringa;

elsif c.object_type = 'TABLE' then
stringa := 'drop table ' || c.object_name || ' cascade constraints';
EXECUTE immediate stringa;
dbms_output.put_line('EXECUTE immediate ' || stringa);

elsif c.object_type = 'SEQUENCE' then
stringa := 'drop sequence ' || c.object_name;
EXECUTE immediate stringa;

elsif c.object_type = 'TRIGGER' then
stringa := 'drop trigger ' || c.object_name;
EXECUTE immediate stringa;
dbms_output.put_line('EXECUTE immediate ' || stringa);

elsif c.object_type = 'SYNONYM' then
stringa := 'drop synonym ' || c.object_name;
EXECUTE immediate stringa;

elsif c.object_type = 'INDEX' then
stringa := 'drop index ' || c.object_name;
EXECUTE immediate stringa;

end if;

end loop;
--PURGE recyclebin
--purge recyclebin;

end;