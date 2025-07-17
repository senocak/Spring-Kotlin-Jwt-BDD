CREATE TABLE public.roles (
    id character varying(255) NOT NULL primary key,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    name character varying(255),
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying])::text[])))
);

CREATE TABLE public.user_roles (
    user_id character varying(255) NOT NULL,
    role_id character varying(255) NOT NULL
);

CREATE TABLE public.users (
    id character varying(255) NOT NULL primary key,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    email character varying(255) unique,
    name character varying(255),
    password character varying(255),
    username character varying(255) unique
);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT "FK7ov27fyo7ebsvada1ej7qkphl" FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT "FKej3jidxlte0r8flpavhiso3g6" FOREIGN KEY (role_id) REFERENCES public.roles(id);

