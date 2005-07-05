drop trigger Est_Id_Type_Trigger;
drop sequence Est_Id_Type_Sequence;
drop table PDF_Y_Vals;
drop table site_Event_Sequence_Info;
drop table Event_Sequence;
drop table Fault_Section_Site;
drop table Paleo_Site_Event;
drop table Normal_Est;
drop table PDF_Est;
drop table Combined_Events_Info;
drop table Fault_Section;
drop table Paleo_Event;
drop table Events_Sequence_Info;
drop table Paleo_Site;
drop table Log_Normal_Est;
drop table XY_Est;
drop table Fault_Model;
drop table Site_Type;
drop table Est_Id_Type;
drop table Contributors;
drop table Aseismic_Slip_Factor;
drop table Log_Type;
drop table Reference;
drop table Est_Type;


CREATE TABLE Est_Type (
  Est_Type_Id INTEGER NOT NULL,
  Est_Name VARCHAR(45) NULL UNIQUE,
  Effective_Date date NULL,
  PRIMARY KEY(Est_Type_Id)
);

insert into Est_Type values(1,'Normal',sysdate);
insert into Est_Type values(2,'LogNormal',sysdate);
insert into Est_Type values(3,'PDF',sysdate);
insert into Est_Type values(4,'Fractile_List',sysdate);
insert into Est_Type values(5,'Integer',sysdate);
insert into Est_Type values(6,'Discrete_Value',sysdate);


CREATE TABLE Reference (
  Reference_Id INTEGER NOT NULL,
  Reference_Name VARCHAR(255) NULL,
  PRIMARY KEY(Reference_Id)
);


CREATE TABLE Log_Type (
  Log_Type_Id INTEGER NOT NULL,
  Log_Base VARCHAR(20) NOT NULL UNIQUE,
  PRIMARY KEY(Log_Type_Id)
);

insert into Log_Type values(1, '10');
insert into Log_Type values(2, 'E');

CREATE TABLE Aseismic_Slip_Factor (
  Aseismic_Slip_Type_Id INTEGER NOT NULL,
  Aseismic_Slip_Type VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Aseismic_Slip_Type_Id)
);

insert into Aseismic_Slip_Factor values(1, 'Aseismic');
insert into Aseismic_Slip_Factor values(0, 'Seismic');

CREATE TABLE Contributors (
  Contributor_Id INTEGER NOT NULL,
  Contributor_Name VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY(Contributor_Id)
);


CREATE TABLE Est_Id_Type (
  Est_Id INTEGER NOT NULL,
  Est_Type_Id INTEGER  NOT NULL,
  Est_Units VARCHAR(45) NULL,
  Comments VARCHAR(255) NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Type_Id) 
    REFERENCES Est_Type(Est_Type_Id)
);

create sequence Est_Id_Type_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Est_Id_Type_Trigger
before insert on Est_Id_Type 
for each row
begin
select Est_Id_Type_Sequence.nextval into :new.Est_Id from dual;
end;
/



CREATE TABLE Site_Type (
  Site_Type_Id INTEGER NOT NULL ,
  Contributor_Id INTEGER NOT NULL,
  Site_Type VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Site_Type_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id)
);


CREATE TABLE Fault_Model (
  Fault_Model_Id INTEGER NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Fault_Model_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Fault_Model_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id) 
);

CREATE TABLE XY_Est (
  X FLOAT NOT NULL,
  Est_Id INTEGER NOT NULL,
  Y FLOAT NOT NULL,
  PRIMARY KEY(X, Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Id_Type(Est_Id)
);

CREATE TABLE Log_Normal_Est (
  Est_Id INTEGER NOT NULL,
  Log_Type_Id INTEGER NOT NULL,
  Median FLOAT NOT NULL,
  Std_Dev FLOAT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Id_Type(Est_Id),
  FOREIGN KEY(Log_Type_Id)
     REFERENCES Log_Type(Log_Type_Id)
);

CREATE TABLE Paleo_Site (
  Site_Id INTEGER NOT NULL,
  Effective_Date date NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Site_Type_Id INTEGER NOT NULL,
  Site_Name VARCHAR(255) NOT NULL,
  Site_Lat FLOAT NOT NULL,
  Site_Lon FLOAT NOT NULL,
  Site_Elevation FLOAT NOT NULL,
  Representative_Strand_Index INTEGER NOT NULL,
  Comments VARCHAR(255) NULL,
  Old_Site_Id INTEGER NULL,
  PRIMARY KEY(Site_Id, Effective_Date, Contributor_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Site_Type_Id)
     REFERENCES Site_Type(Site_Type_Id)
);

CREATE TABLE Events_Sequence_Info (
  Sequence_Id INTEGER NOT NULL,
  Reference_Id INTEGER NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Start_Time_Est_Id INTEGER NOT NULL,
  End_Time_Est_Id INTEGER  NOT NULL,
  Sequence_Name VARCHAR(255) NOT NULL,
  Effective_Date date NOT NULL,
  Comments VARCHAR(255) NOT NULL,
  PRIMARY KEY(Sequence_Id, Contributor_Id, Effective_Date),  
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
 FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id),
 FOREIGN KEY(Start_Time_Est_Id)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(End_Time_Est_Id)
     REFERENCES Est_Id_Type(Est_Id)
);

CREATE TABLE Paleo_Event (
  Event_Id INTEGER NOT NULL ,
  Reference_Id INTEGER NOT NULL,
  Contributor_Id INTEGER  NOT NULL,
  Event_Date_Est_Id INTEGER  NOT NULL,
  Displacement_Est_Id INTEGER NOT NULL,
  Event_Name VARCHAR(255) NOT NULL,
  Effective_Date date NOT NULL,
  Comments VARCHAR(255) NULL,
  PRIMARY KEY(Event_Id, Contributor_Id, Effective_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id),
 FOREIGN KEY(Event_Date_Est_Id)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Displacement_Est_Id)
     REFERENCES Est_Id_Type(Est_Id)
);


CREATE TABLE Fault_Section (
  Section_Id INTEGER  NOT NULL,
  Fault_Id INTEGER  NOT NULL,
  Fault_Model_Id INTEGER  NOT NULL,
  Pref_Slip_Rate INTEGER  NOT NULL,
  Pref_Dip INTEGER  NOT NULL,
  Pref_Rake INTEGER  NOT NULL,
  Pref_Upper_Depth INTEGER  NOT NULL,
  Pref_Lower_Depth INTEGER  NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Name VARCHAR(255) NOT NULL,
  Effective_Date date NOT NULL,
  Comments VARCHAR(255) NULL,
  Fault_Trace VARCHAR(255) NOT NULL,
  Aseismic_Slip_Factor_Index INTEGER  NOT NULL,
  PRIMARY KEY(Section_Id, Fault_Id, Fault_Model_Id, Effective_Date),
  FOREIGN KEY(Fault_Model_Id)
    REFERENCES Fault_Model(Fault_Model_Id),
 FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
 FOREIGN KEY(Pref_Slip_Rate)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Pref_Dip)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Pref_Rake)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Pref_Upper_Depth)
     REFERENCES Est_Id_Type(Est_Id), 
 FOREIGN KEY(Pref_Lower_Depth)
     REFERENCES Est_Id_Type(Est_Id)
);

CREATE TABLE Combined_Events_Info (
  Info_Id INTEGER  NOT NULL,
  Site_Id INTEGER  NOT NULL,
  Site_Contributor_Id INTEGER  NOT NULL,
  Site_Effective_Date date NOT NULL,  
  Effective_Date date NOT NULL,
  Reference_Id INTEGER  NOT NULL,
  Contributor_Id INTEGER  NOT NULL,
  Start_Time_Est_Id INTEGER  NOT NULL,
  End_Time_Est_Id INTEGER  NOT NULL,
  Total_Slip_Est_Id INTEGER  NOT NULL,
  Slip_Rate_Est_Id INTEGER  NOT NULL,
  Num_Events_Est_Id INTEGER  NOT NULL,
  Aseismic_Slip_Type_Id INTEGER  NOT NULL,
  Info_Name VARCHAR(255) NOT NULL,
  Comments VARCHAR(255) NULL,
  Dated_Feature_Comments VARCHAR(255) NULL,
  PRIMARY KEY(Info_Id, Site_Id, Effective_Date, Contributor_Id),
  FOREIGN KEY (Site_Id, Site_Contributor_Id, Site_Effective_Date) 
    REFERENCES Paleo_Site(Site_Id, Contributor_Id, Effective_Date),
 FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
 FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id),
 FOREIGN KEY(Aseismic_Slip_Type_Id)
   REFERENCES Aseismic_Slip_Factor(Aseismic_Slip_Type_Id),
 FOREIGN KEY(Start_Time_Est_Id)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(End_Time_Est_Id)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Total_Slip_Est_Id)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Slip_Rate_Est_Id)
     REFERENCES Est_Id_Type(Est_Id),
 FOREIGN KEY(Num_Events_Est_Id)
     REFERENCES Est_Id_Type(Est_Id)
);

CREATE TABLE PDF_Est (
  Est_Id INTEGER  NOT NULL,
  Min_X FLOAT NULL,
  Delta_X FLOAT NULL,
  Num INTEGER  NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Id_Type(Est_Id)
);

CREATE TABLE Normal_Est (
  Est_Id INTEGER  NOT NULL,
  Mean FLOAT NULL,
  Std_Dev FLOAT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Id_Type(Est_Id)
);

CREATE TABLE Paleo_Site_Event (
  Site_Id INTEGER  NOT NULL,
  Site_Effective_Date date NOT NULL,
  Site_Contributor_Id INTEGER  NOT NULL,
  Event_Id INTEGER  NOT NULL,
  Event_Effective_Date date NOT NULL,
  Event_Contributor_Id INTEGER  NOT NULL,
  PRIMARY KEY(Site_Id, Event_Id, Site_Effective_Date, Site_Contributor_Id, Event_Effective_Date, Event_Contributor_Id),  
  FOREIGN KEY(Site_Id, Site_Effective_Date, Site_Contributor_Id)
    REFERENCES Paleo_Site(Site_Id, Effective_Date, Contributor_Id),
  FOREIGN KEY(Event_Id, Event_Effective_Date, Event_Contributor_Id)
    REFERENCES Paleo_Event(Event_Id, Effective_Date, Contributor_Id)
);


CREATE TABLE Fault_Section_Site (
  Fault_Id INTEGER  NOT NULL,
  Section_Id INTEGER  NOT NULL,
  Fault_Section_Effective_Date date NOT NULL,
  Fault_Model_Id INTEGER  NOT NULL,  
  Site_Contributor_Id INTEGER  NOT NULL,
  Site_Id INTEGER  NOT NULL,
  Site_Effective_Date date NOT NULL,  
  PRIMARY KEY(Site_Id, Fault_Id, Section_Id, Fault_Section_Effective_Date, Site_Contributor_Id, Fault_Model_Id, Site_Effective_Date),
  FOREIGN KEY(Site_Id, Site_Contributor_Id, Site_Effective_Date)
   REFERENCES Paleo_Site(Site_Id, Contributor_Id, Effective_Date),
  FOREIGN KEY(Fault_Id, Section_Id, Fault_Model_Id, Fault_Section_Effective_Date)
   REFERENCES Fault_Section(Fault_Id, Section_Id, Fault_Model_Id, Effective_Date)
);


CREATE TABLE Event_Sequence (
  Event_Id INTEGER  NOT NULL,
  Event_Contributor_Id  INTEGER  NOT NULL, 
  Event_Effective_Date date NOT NULL,
  Sequence_Id INTEGER  NOT NULL,
  Sequence_Contributor_Id  INTEGER  NOT NULL, 
  Sequence_Effective_Date date NOT NULL,
  Missed_Prob FLOAT NOT NULL,
  Event_Index_In_Sequence INTEGER  NOT NULL,
  PRIMARY KEY(Event_Id, Event_Contributor_Id, Event_Effective_Date, Sequence_Id, Sequence_Contributor_Id, Sequence_Effective_Date),
  FOREIGN KEY(Event_Id, Event_Contributor_Id, Event_Effective_Date)
   REFERENCES Paleo_Event(Event_Id, Contributor_Id, Effective_Date),
  FOREIGN KEY(Sequence_Id, Sequence_Contributor_Id, Sequence_Effective_Date)
   REFERENCES Events_Sequence_Info(Sequence_Id, Contributor_Id, Effective_Date)  
);

CREATE TABLE Site_Event_Sequence_Info (
  Sequence_Id INTEGER  NOT NULL,
  Sequence_Contributor_Id  INTEGER  NOT NULL, 
  Sequence_Effective_Date date NOT NULL,
  Site_Contributor_Id INTEGER  NOT NULL,
  Site_Id INTEGER  NOT NULL,
  Site_Effective_Date date NOT NULL,  
  Contributor_Id INTEGER  NOT NULL,
  Sequence_Wt FLOAT NOT NULL,
  PRIMARY KEY(Sequence_Id, Site_Id, Contributor_Id),
  FOREIGN KEY(Site_Id, Site_Contributor_Id, Site_Effective_Date)
   REFERENCES Paleo_Site(Site_Id, Contributor_Id, Effective_Date),
  FOREIGN KEY(Sequence_Id, Sequence_Contributor_Id, Sequence_Effective_Date)
   REFERENCES Events_Sequence_Info(Sequence_Id, Contributor_Id, Effective_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id)  
);

CREATE TABLE PDF_Y_Vals (
  Y_Vals_Id INTEGER  NOT NULL,
  Est_Id INTEGER  NOT NULL,
  Y_Val FLOAT NULL,
  PRIMARY KEY(Y_Vals_Id, Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES PDF_Est(Est_Id) 
);