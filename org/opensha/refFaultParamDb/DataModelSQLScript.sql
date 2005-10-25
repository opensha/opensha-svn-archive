drop View Vw_Paleo_Site_Chars;
drop table Event_Sequence_Event_List;
drop table Event_Sequence_References;
drop trigger Event_Sequence_Trigger;
drop sequence Event_Sequence_Sequence;
drop table Event_Sequence;
drop sequence Paleo_Event_Sequence;
drop trigger Paleo_Event_Trigger;
drop table Paleo_Event_References;
drop table Paleo_Event;
drop table Combined_Events_References;
drop sequence Combined_Events_Sequence;
drop trigger Combined_Events_Trigger;
drop table Combined_Events_Slip_Rate_Info;
drop table Combined_Events_Displacement_Info;
drop table Combined_Events_Num_Events_Info;
drop table Combined_Events_Info;
drop table Time_Estimate_Info;
drop table Exact_Time_Info;
drop table Time_Instances_References;
drop trigger Time_Instances_Trigger;
drop sequence Time_Instances_Sequence;
drop table Time_Instances;
drop table Time_Type;
drop table Paleo_Site_References;
drop trigger Paleo_Site_Trigger;
drop sequence Paleo_Site_Sequence;
drop table Paleo_Site;
drop trigger Site_type_Trigger;
drop sequence Site_type_Sequence;
drop table site_type;
drop table Fault_Model_Section;
drop trigger Fault_Model_Trigger;
drop sequence Fault_Model_Sequence;
drop table Fault_Model;
drop table Fault_Section;
drop trigger Section_Source_Trigger;
drop sequence Section_Source_Sequence;
drop table Section_Source;
drop table PDF_Est;
drop table Log_Normal_Est;
drop table Log_type;
drop table XY_Est;
drop table Normal_Est;
drop trigger Est_Instances_Trigger;
drop sequence Est_Instances_Sequence;
drop table Est_Instances;
drop table Est_Type;
drop trigger Contributors_Trigger;
drop sequence Contributors_Sequence;
drop table Contributors;
drop trigger Reference_Trigger;
drop sequence Reference_Sequence;
drop table Reference;
drop table Site_Representations;

CREATE TABLE Site_Representations(
  Site_Representation_Id INTEGER NOT NULL,
  Site_Representation_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY (Site_Representation_Id)
);


CREATE TABLE Reference (
  Reference_Id INTEGER NOT NULL,
  QFault_Reference_Id INTEGER NULL, 
  Ref_Auth VARCHAR(1000) NULL,
  Ref_Year VARCHAR(50) NULL, 
  Full_Bibliographic_Reference VARCHAR(4000) NULL,
  PRIMARY KEY(Reference_Id)
);

create sequence Reference_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Reference_Trigger
before insert on Reference 
for each row
begin
if :new.Reference_Id  is null then
select  Reference_Sequence.nextval into :new.Reference_Id  from dual;
end if;
end;
/



CREATE TABLE Contributors (
  Contributor_Id INTEGER NOT NULL,
  Contributor_Name VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY(Contributor_Id)
);


create sequence Contributors_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Contributors_Trigger
before insert on Contributors 
for each row
begin
if :new.Contributor_Id  is null then
select  Contributors_Sequence.nextval into :new.Contributor_Id  from dual;
end if;
end;
/



CREATE TABLE Est_Type (
  Est_Type_Id INTEGER NOT NULL,
  Est_Name VARCHAR(45) NOT NULL UNIQUE,
  Entry_Date date NOT NULL,
  PRIMARY KEY(Est_Type_Id)
);




CREATE TABLE Est_Instances (
  Est_Id INTEGER NOT NULL,
  Est_Type_Id INTEGER  NOT NULL,
  Est_Units VARCHAR(45) NULL,
  Comments VARCHAR(255) NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Type_Id) 
    REFERENCES Est_Type(Est_Type_Id)
);

create sequence Est_Instances_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Est_Instances_Trigger
before insert on Est_Instances 
for each row
begin
if :new.Est_Id is null then
select Est_Instances_Sequence.nextval into :new.Est_Id from dual;
end if;
end;
/


CREATE TABLE Normal_Est (
  Est_Id INTEGER  NOT NULL,
  Mean NUMBER(9,3) NOT NULL,
  Std_Dev NUMBER(9,3) NOT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id)
);

CREATE TABLE XY_Est (
  X NUMBER(9,3) NOT NULL,
  Est_Id INTEGER NOT NULL,
  Y NUMBER(9,3) NULL,
  PRIMARY KEY(X, Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id)
);

CREATE TABLE Log_Type (
  Log_Type_Id INTEGER NOT NULL,
  Log_Base VARCHAR(20) NOT NULL UNIQUE,
  PRIMARY KEY(Log_Type_Id)
);



CREATE TABLE Log_Normal_Est (
  Est_Id INTEGER NOT NULL,
  Log_Type_Id INTEGER NOT NULL,
  Median NUMBER(9,3) NOT NULL,
  Std_Dev NUMBER(9,3) NOT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Log_Type_Id)
     REFERENCES Log_Type(Log_Type_Id)
);

CREATE TABLE PDF_Est (
  Est_Id INTEGER  NOT NULL,
  Min_X NUMBER(9,3) NOT NULL,
  Delta_X NUMBER(9,3) NOT NULL,
  Num INTEGER  NOT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id)
);



CREATE TABLE Section_Source (
  Section_Source_Id INTEGER NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Section_Source_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Section_Source_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id) 
);


create sequence Section_Source_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Section_Source_Trigger
before insert on Section_Source 
for each row
begin
if :new.Section_Source_Id  is null then
select  Section_Source_Sequence.nextval into :new.Section_Source_Id  from dual;
end if;
end;
/



CREATE TABLE Fault_Section (
  Section_Id INTEGER  NOT NULL,
  Fault_Id INTEGER  NOT NULL,
  Section_Source_Id INTEGER  NOT NULL,
  Ave_Long_Term_Slip_Rate_Est INTEGER  NOT NULL,
  Ave_Dip_Est INTEGER  NOT NULL,
  Ave_Rake_Est INTEGER  NOT NULL,
  Ave_Upper_Depth_Est INTEGER  NOT NULL,
  Ave_Lower_Depth_Est INTEGER  NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Name VARCHAR(255) NOT NULL,
  Entry_Date date NOT NULL,
  Comments VARCHAR(1000) NULL,
  Fault_Trace VARCHAR(255) NOT NULL,
  Aseismic_Slip_Factor_Est INTEGER  NOT NULL,
  PRIMARY KEY(Section_Id, Fault_Id, Section_Source_Id, Entry_Date),
  FOREIGN KEY(Section_Source_Id)
    REFERENCES Section_Source(Section_Source_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Ave_Long_Term_Slip_Rate_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Ave_Dip_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Ave_Rake_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Ave_Upper_Depth_Est)
     REFERENCES Est_Instances(Est_Id), 
  FOREIGN KEY(Ave_Lower_Depth_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Aseismic_Slip_Factor_Est)
     REFERENCES Est_Instances(Est_Id)
);


CREATE TABLE Fault_Model (
  Fault_Model_Id INTEGER NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Fault_Model_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Fault_Model_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id) 
);


create sequence Fault_Model_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Fault_Model_Trigger
before insert on Fault_Model 
for each row
begin
if :new.Fault_Model_Id  is null then
select  Fault_Model_Sequence.nextval into :new.Fault_Model_Id  from dual;
end if;
end;
/

CREATE TABLE Fault_Model_Section (
  Fault_Model_Id INTEGER NOT NULL,
  Section_Id INTEGER  NOT NULL,
  Fault_Id INTEGER  NOT NULL,
  Section_Source_Id INTEGER  NOT NULL,
  Section_Entry_Date date NOT NULL,
  PRIMARY KEY(Fault_Model_Id, Section_Id, Fault_Id, Section_Source_Id, Section_Entry_Date),
  FOREIGN KEY(Fault_Model_Id)
     REFERENCES  Fault_Model(Fault_Model_Id),
  FOREIGN KEY(Section_Id, Fault_Id, Section_Source_Id, Section_Entry_Date)
     REFERENCES Fault_Section(Section_Id, Fault_Id, Section_Source_Id, Entry_Date)
);


CREATE TABLE Site_Type (
  Site_Type_Id INTEGER NOT NULL ,
  Contributor_Id INTEGER NOT NULL,
  Site_Type VARCHAR(255) NOT NULL UNIQUE,
  General_Comments VARCHAR(1000) NULL,
  PRIMARY KEY(Site_Type_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id)
);

create sequence Site_Type_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Site_Type_Trigger
before insert on Site_Type 
for each row
begin
if :new.Site_Type_Id  is null then
select  Site_Type_Sequence.nextval into :new.Site_Type_Id  from dual;
end if;
end;
/


CREATE TABLE Paleo_Site (
  Site_Id INTEGER NOT NULL,
  Fault_Id INTEGER NOT NULL,
  Entry_Date date NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Site_Type_Id INTEGER NOT NULL,
  Site_Name VARCHAR(255) NULL,
  Site_Location1 MDSYS.SDO_GEOMETRY,
  Site_Location2 MDSYS.SDO_GEOMETRY,
  Representative_Strand_Index INTEGER NOT NULL,
  General_Comments VARCHAR(1000) NULL,
  Old_Site_Id VARCHAR(20) NULL,
  PRIMARY KEY(Site_Id, Entry_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Site_Type_Id)
     REFERENCES Site_Type(Site_Type_Id),
  FOREIGN KEY(Representative_Strand_Index)
     REFERENCES Site_Representations(Site_Representation_Id)
);

create sequence Paleo_Site_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Paleo_Site_Trigger
before insert on Paleo_Site 
for each row
begin
if :new.Site_Id  is null then
select  Paleo_Site_Sequence.nextval into :new.Site_Id  from dual;
end if;
end;
/


CREATE TABLE Paleo_Site_References (
  Site_Id INTEGER NOT NULL,
  Entry_Date date NOT NULL,
  Reference_Id INTEGER  NOT NULL,
  PRIMARY KEY(Site_Id, Entry_Date,Reference_Id),
  FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id),
  FOREIGN KEY(Site_Id, Entry_Date) 
     REFERENCES Paleo_Site(Site_Id, Entry_Date)
);
  


CREATE TABLE Time_Type (
  Time_Type_Id INTEGER NOT NULL,
  Time_Type_Description VARCHAR(255) NOT NULL UNIQUE,
  Entry_Date date NOT NULL,
  PRIMARY KEY(Time_Type_Id)
);


CREATE TABLE Time_Instances (
  Time_Id INTEGER NOT NULL,
  Time_Type_Id INTEGER  NOT NULL,
  Comments VARCHAR(1000) NULL,
  PRIMARY KEY(Time_Id),
  FOREIGN KEY(Time_Type_Id) 
    REFERENCES Time_Type(Time_Type_Id)
);

create sequence Time_Instances_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Time_Instances_Trigger
before insert on Time_Instances 
for each row
begin
if :new.Time_Id is null then
select Time_Instances_Sequence.nextval into :new.Time_Id from dual;
end if;
end;
/

CREATE TABLE Time_Instances_References (
  Time_Id INTEGER NOT NULL,
  Reference_Id INTEGER NOT NULL,
  PRIMARY KEY(Time_Id, Reference_Id),
  FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id),
  FOREIGN KEY(Time_Id)
     REFERENCES Time_Instances(Time_Id)
);
  

CREATE TABLE Exact_Time_Info (
   Time_Instance_Id INTEGER NOT NULL,
   Year INTEGER NOT NULL,
   Month INTEGER NOT NULL,
   Day INTEGER NOT NULL,
   Hour INTEGER NOT NULL,
   Minute INTEGER NOT NULL,
   Second INTEGER NOT NULL,
   Era VARCHAR(2) NOT NULL,
   PRIMARY KEY(Time_Instance_Id),
   FOREIGN KEY(Time_Instance_Id) 
      REFERENCES Time_Instances(Time_Id)
);

CREATE TABLE Time_Estimate_Info(
   Time_Instance_Id INTEGER NOT NULL,
   Time_Est_Id INTEGER NOT NULL,
   Is_Ka char(1) NOT NULL,
   Era VARCHAR(2) NULL,
   Zero_Year INTEGER NULL,
   PRIMARY KEY(Time_Instance_Id),
   FOREIGN KEY(Time_Instance_Id) 
      REFERENCES Time_Instances(Time_Id),
   FOREIGN KEY(Time_Est_Id)
      REFERENCES Est_Instances(Est_Id)
);



CREATE TABLE Combined_Events_Info (
  Info_Id INTEGER  NOT NULL,
  Site_Id INTEGER  NOT NULL, 
  Site_Entry_Date date NOT NULL,  
  Entry_Date date NOT NULL,
  Contributor_Id INTEGER  NOT NULL,
  Start_Time_Id INTEGER  NOT NULL,
  End_Time_Id INTEGER  NOT NULL,	
  Dated_Feature_Comments VARCHAR(255) NULL,
  PRIMARY KEY(Info_Id, Entry_Date),
  FOREIGN KEY (Site_Id, Site_Entry_Date) 
    REFERENCES Paleo_Site(Site_Id, Entry_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Start_Time_Id)
     REFERENCES Time_Instances(Time_Id),
  FOREIGN KEY(End_Time_Id)
     REFERENCES Time_Instances(Time_Id),
  FOREIGN KEY(Total_Slip_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Slip_Rate_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Num_Events_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Slip_Aseismic_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Disp_Aseismic_Est_Id)
     REFERENCES Est_Instances(Est_Id)
);

CREATE TABLE Combined_Slip_Rate_Info (
 Info_Id INTEGER  NOT NULL,
 Entry_Date date NOT NULL,
 Slip_Rate_Est_Id INTEGER  NULL,
 Slip_Aseismic_Est_Id INTEGER  NULL,
 Slip_Rate_Comments VARCHAR(1000) NULL,
 PRIMARY KEY(Info_Id, Entry_Date),
 FOREIGN KEY(Info_Id, Entry_Date)
     REFERENCES Combined_Events_Info(Info_Id, Entry_Date)
);

CREATE TABLE Combined_Displacement_Info (
 Info_Id INTEGER  NOT NULL,
 Entry_Date date NOT NULL,
 Total_Slip_Est_Id INTEGER  NULL,
 Disp_Aseismic_Est_Id INTEGER  NULL,
 Total_Slip_Comments VARCHAR(1000) NULL,
 PRIMARY KEY(Info_Id, Entry_Date),
 FOREIGN KEY(Info_Id, Entry_Date)
     REFERENCES Combined_Events_Info(Info_Id, Entry_Date)
);

CREATE TABLE Combined_Num_Events_Info (
 Info_Id INTEGER  NOT NULL,
 Entry_Date date NOT NULL,
 Num_Events_Est_Id INTEGER  NULL,
 Num_Events_Comments VARCHAR(1000) NULL, 
 PRIMARY KEY(Info_Id, Entry_Date),
 FOREIGN KEY(Info_Id, Entry_Date)
     REFERENCES Combined_Events_Info(Info_Id, Entry_Date)
);



CREATE TABLE Combined_Events_References (
 combined_Events_Id INTEGER  NOT NULL,
 combined_Events_Entry_Date date NOT NULL,
 Reference_Id INTEGER  NOT NULL,
 PRIMARY KEY (combined_Events_Id, combined_Events_Entry_Date, Reference_Id),
 FOREIGN KEY (combined_Events_Id,  combined_Events_Entry_Date)
   REFERENCES Combined_Events_Info(Info_Id,  Entry_Date),
 FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id)
);

create sequence Combined_Events_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Combined_Events_Trigger
before insert on Combined_Events_Info 
for each row
begin
if :new.Info_Id is null then
select Combined_Events_Sequence.nextval into :new.Info_Id from dual;
end if;
end;
/
 

CREATE TABLE Paleo_Event (
  Event_Id INTEGER NOT NULL ,
  Event_Name VARCHAR(255) NOT NULL UNIQUE,
  Site_Id INTEGER  NOT NULL,  
  Site_Entry_Date date NOT NULL,
  Contributor_Id INTEGER  NOT NULL,
  Event_Date_Est_Id INTEGER  NOT NULL,
  Displacement_Est_Id INTEGER NOT NULL,
  Entry_Date date NOT NULL,
  General_Comments VARCHAR(1000) NULL,
  PRIMARY KEY(Event_Id, Entry_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Event_Date_Est_Id)
     REFERENCES Time_Instances(Time_Id),
  FOREIGN KEY(Displacement_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY (Site_Id, Site_Entry_Date) 
    REFERENCES Paleo_Site(Site_Id, Entry_Date)
);


CREATE TABLE Paleo_Event_References (
 Paleo_Event_Id INTEGER  NOT NULL,
 Paleo_Event_Entry_Date DATE NOT NULL,
 Reference_Id INTEGER  NOT NULL,
 PRIMARY KEY (Paleo_Event_Id, Paleo_Event_Entry_Date, Reference_Id),
 FOREIGN KEY (Paleo_Event_Id,  Paleo_Event_Entry_Date)
   REFERENCES Paleo_Event(Event_Id, Entry_Date),
 FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id)
);

create sequence Paleo_Event_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Paleo_Event_Trigger
before insert on Paleo_Event 
for each row
begin
if :new.Event_Id is null then
select Paleo_Event_Sequence.nextval into :new.Event_Id from dual;
end if;
end;
/


CREATE TABLE Event_Sequence (
  Sequence_Id INTEGER  NOT NULL,
  Entry_Date date NOT NULL,
  Sequence_Id INTEGER NOT NULL,
  Sequence_Name VARCHAR(255) NOT NULL UNIQUE,
  General_Comments VARCHAR(1000) NOT NULL,
  Sequence_Probability NUMBER(9,3) NOT NULL,
  PRIMARY KEY(Sequence_Id, Entry_Date),  
  FOREIGN KEY(Sequence_Id, Entry_Date)
     REFERENCES Combined_Events_Info(Info_Id, Entry_Date)
);



CREATE TABLE Event_Sequence_Event_List (
  Event_Id INTEGER  NOT NULL,
  Event_Entry_Date DATE NOT NULL,
  Sequence_Id INTEGER  NOT NULL,
  Sequence_Entry_Date date NOT NULL,
  Missed_Prob NUMBER(9,3) NOT NULL,
  Event_Index_In_Sequence INTEGER  NOT NULL,
  PRIMARY KEY(Sequence_Id,  Sequence_Entry_Date, Event_Index_In_Sequence),
  FOREIGN KEY(Event_Id,  Event_Entry_Date)
   REFERENCES Paleo_Event(Event_Id,  Entry_Date),
  FOREIGN KEY(Sequence_Id, Sequence_Entry_Date)
   REFERENCES Event_Sequence(Sequence_Id, Entry_Date)  
);



insert into Reference (Ref_AUth, Ref_Year, Full_Bibliographic_Reference) values ('Short Citation 1', '2001', 'Full Bibliographic Reference 1');
insert into Reference (Ref_AUth, Ref_Year, Full_Bibliographic_Reference) values ('Short Citation 2', '2002', 'Full Bibliographic Reference 2');
insert into Reference (Ref_AUth, Ref_Year, Full_Bibliographic_Reference) values ('Short Citation 3', '2003', 'Full Bibliographic Reference 3');
insert into Reference (Ref_AUth, Ref_Year, Full_Bibliographic_Reference) values ('Short Citation 4', '2004', 'Full Bibliographic Reference 4');


insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (1, 'Most Significant Strand');
insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (2, 'One of Several Strands');
insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (3, 'Entire Fault');
insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (4, 'Unknown');

insert into Contributors(Contributor_Id, Contributor_name) values (1, 'fault_sandbox');
insert into Contributors(Contributor_Id, Contributor_name) values (2, 'vipin');

insert into Est_Type values(1,'NormalEstimate',sysdate);
insert into Est_Type values(2,'LogNormalEstimate',sysdate);
insert into Est_Type values(3,'PDF_Estimate',sysdate);
insert into Est_Type values(4,'FractileListEstimate',sysdate);
insert into Est_Type values(5,'IntegerEstimate',sysdate);
insert into Est_Type values(6,'DiscreteValueEstimate',sysdate);

insert into Log_Type values(1, '10');
insert into Log_Type values(2, 'E');

insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Trench', 'Trench Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Geologic', 'Geologic Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Survey/Cultural', 'Survey/Cultural Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Between Locations', 'Between Locations Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Unknown', 'Unknown Site Type');


insert into Time_Type values (1, 'Exact Time', sysdate);
insert into Time_Type values (2, 'Time Estimate', sysdate);


create VIEW Vw_Paleo_Site_Chars AS
select ps.Site_Id as Site_Id,
       ps.Fault_Id as Fault_Id, 
       ps.Entry_Date as Entry_Date,
       st.Site_Type Site_Type,
       ps.Site_Name as Site_Name,
       ps.Site_Location1 as Site_Location1,
       ps.Site_Location2 as Site_Location2,
       sr.Site_Representation_Name as Site_Representation_Name,
       ps.General_Comments as General_comments,
       ps.Old_Site_Id as Old_site_Id,
       contrib.Contributor_Name as Contributor_Name
FROM  Paleo_Site ps, Site_Type st, Contributors contrib, Site_Representations sr, (select max(entry_date) as maxdate,site_id from paleo_site group by site_id) maxresults where ps.site_id=maxresults.site_id and ps.entry_date=maxresults.maxdate and ps.Site_type_id=st.site_type_id and ps.Representative_Strand_Index=sr.Site_Representation_Id and ps.Contributor_Id=contrib.Contributor_Id;

INSERT into Reference ( Ref_Auth, Ref_Year,Full_Bibliographic_Reference, QFault_Reference_Id)
select Ref_Auth, Ref_Year, Reference_Tx, Ref_Num from QFault_References;


commit;


