use moviedb;
DROP PROCEDURE IF EXISTS add_movie;
DELIMITER //

CREATE PROCEDURE add_movie(
    IN movieTitle VARCHAR(100),
    IN movieYear INTEGER,
    IN movieDirector VARCHAR(100),
    IN starName VARCHAR(100),
    IN genreName VARCHAR(32)
)
BEGIN
    DECLARE movieId, starId, genreId VARCHAR(10);
    DECLARE maxMovieId, maxStarId, maxGenreId INT;
    
    -- Check if movie already exists
    SELECT id INTO movieId
    FROM movies
    WHERE title = movieTitle AND year = movieYear AND director = movieDirector;

    IF movieId IS NULL THEN
        -- Generate new movie ID
        SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) INTO maxMovieId FROM movies;
        SET movieId = CONCAT('tt', LPAD(maxMovieId + 1, 7, '0'));

        -- Check if star already exists
        SELECT id INTO starId
        FROM stars
        WHERE name = starName;

        -- If star doesn't exist, create it
        IF starId IS NULL THEN
            -- Generate new star ID
            SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) INTO maxStarId FROM stars;
            SET starId = CONCAT('nm', LPAD(maxStarId + 1, 7, '0'));

            INSERT INTO stars (id, name)
            VALUES (starId, starName);
        END IF;

        -- Check if genre already exists
        SELECT id INTO genreId
        FROM genres
        WHERE name = genreName;

        -- If genre doesn't exist, create it
        IF genreId IS NULL THEN
            -- Generate new genre ID
            SELECT MAX(id) INTO maxGenreId FROM genres;
            SET genreId = maxGenreId + 1;

            INSERT INTO genres (id, name)
            VALUES (genreId, genreName);
        END IF;

        -- Insert the new movie
        INSERT INTO movies (id, title, year, director)
        VALUES (movieId, movieTitle, movieYear, movieDirector);

        -- Link star and genre to the movie
        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (starId, movieId);

        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (genreId, movieId);
        
        INSERT INTO ratings (movieId, rating, numVotes)
        VALUES (movieId, 0.0, 0);

        SELECT CONCAT('Success! Movie ID: ', movieId, ' Star ID: ', starId, ' genre ID: ', genreId) AS Status;
    ELSE
        SELECT 'Error! Duplicated movie!' AS Status;
    END IF;
END //

DELIMITER ;