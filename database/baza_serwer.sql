create table Categories(
CategoryId int constraint PK_CATEGORY primary key identity(1,1),
Name nvarchar(100),
Version int);

create table Recipes(
RecipeId int constraint PK_RECIPES primary key identity(1,1),
Name nvarchar (100),
Description nvarchar (5000),
Photo nvarchar (100),
ModificationDate datetime2,
Version int);

create table RecipesCategories(
RecipeId int constraint FK_LINK_RECIPES_CATEGORIES references Recipes on delete cascade,
CategoryId int constraint FK_LINK_CATEGORIES_RECIPES references Categories on delete cascade,
primary key clustered (RecipeId, CategoryId));

create table RecipesRating(
RecipesRatingId int constraint PK_RECIPES_RATING primary key identity(1,1),
RecipeId int constraint FK_LINK_RECIPES_RATING references Recipes on delete cascade,
Rating int,
);

create index Index_RecipeName on Recipes (Name ASC);

create index Index_RecipeId on RecipesCategories (RecipeId ASC);

create index Index_RecipesRating_RecipeId on RecipesRating (RecipeId ASC);