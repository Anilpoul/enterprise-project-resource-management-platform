CREATE TABLE departments
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE designations
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_profiles
(
    id UUID PRIMARY KEY,

    auth_user_id UUID NOT NULL UNIQUE,

    employee_code VARCHAR(50) UNIQUE,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,

    phone_number VARCHAR(20),

    department_id UUID,
    designation_id UUID,

    manager_id UUID,

    profile_image_url VARCHAR(500),

    joining_date DATE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_department
        FOREIGN KEY (department_id)
            REFERENCES departments(id),

    CONSTRAINT fk_designation
        FOREIGN KEY (designation_id)
            REFERENCES designations(id)
);