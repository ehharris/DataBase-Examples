CREATE TABLE Member (
    MemberID    INT             NOT NULL,
    FName  VARCHAR(16),
    LName  VARCHAR(16),
    Gender      ENUM('M','F'),
    dob         VARCHAR(10),
    PRIMARY KEY (MemberID)
);

CREATE TABLE Author (
    AuthorID   INT    NOT NULL,
    FName  VARCHAR(16),
    LName  VARCHAR(16),
    PRIMARY KEY (AuthorID)
);

CREATE TABLE Publisher (
    PubID      INT             NOT NULL,
    Pub_name    VARCHAR(30),
    PRIMARY KEY (PubID)
);

CREATE TABLE Book (
    ISBN        VARCHAR(14)     NOT NULL,
    Title       VARCHAR(50),
    Pub_yr      VARCHAR(10),
    PubID       INT     NOT NULL,
    PRIMARY KEY (ISBN),
    FOREIGN KEY (PubID) REFERENCES Publisher(PubID) ON DELETE CASCADE
);

CREATE TABLE Written (
    ISBN        VARCHAR(14)     NOT NULL,
    AuthorID   INT    NOT NULL,
    FOREIGN KEY (ISBN) REFERENCES Book (ISBN) ON DELETE CASCADE,
    FOREIGN KEY (AuthorID) REFERENCES Author (AuthorID) ON DELETE CASCADE,
    PUBLIC KEY (ISBN, AuthorID)
);

CREATE TABLE Phone (
    PNumber     VARCHAR(12)            NOT NULL,
    Type    ENUM ('c','o','h'),
    PRIMARY KEY (PNumber)
);

CREATE TABLE Borrowed (
    MemberID    INT            NOT NULL,
    ISBN        VARCHAR(14)    NOT NULL,
    CheckOut    VARCHAR(10)               NOT NULL,
    CheckIn     VARCHAR(10),
    FOREIGN KEY (MemberID) REFERENCES Member (MemberID) ON DELETE CASCADE,
    FOREIGN KEY (ISBN) REFERENCES Book (ISBN) ON DELETE CASCADE,
    PRIMARY KEY (MemberID, ISBN)
);

CREATE TABLE PubPhone (
    PubID      INT          NOT NULL,
    PNumber    VARCHAR(12)  NOT NULL,
    FOREIGN KEY (PubID) REFERENCES Publisher (PubID) ON DELETE CASCADE,
    FOREIGN KEY (PNumber) REFERENCES Phone (PNumber) ON DELETE CASCADE,
    PRIMARY KEY (PubID, PNumber)
);

CREATE TABLE AuthPhone (
    AuthorID   INT          NOT NULL,
    PNumber    VARCHAR(12)  NOT NULL,
    FOREIGN KEY (AuthorID) REFERENCES Author (AuthorID) ON DELETE CASCADE,
    FOREIGN KEY (PNumber) REFERENCES Phone (PNumber) ON DELETE CASCADE,
    PRIMARY KEY (AuthorID, PNumber)
);

CREATE TABLE Library (
    Name    VARCHAR(20)     NOT NULL,
    Street  VARCHAR(60),
    City    VARCHAR(20),
    State   VARCHAR(20),
    PRIMARY KEY (Name)
);

CREATE TABLE Shelf (
    ShelfNum    INT     NOT NULL,
    FloorNum    INT     NOT NULL,
    LibName     VARCHAR(20)     NOT NULL,
    FOREIGN KEY (LibName) REFERENCES Library(Name) ON DELETE CASCADE,
    PRIMARY KEY (ShelfNum)
);

CREATE TABLE Stored (
    ShelfNum    INT     NOT NULL,
    ISBN        VARCHAR(14)     NOT NULL,
    Copies      INT     NOT NULL,
    FOREIGN KEY (ShelfNum) REFERENCES Shelf(ShelfNum) ON DELETE CASCADE,
    FOREIGN KEY (ISBN) REFERENCES Book (ISBN) ON DELETE CASCADE,
    PRIMARY KEY (ShelfNum, ISBN)
);






    
    
