-- Run once on existing MySQL environments before deploying the route candidate DB storage change.
-- The application now allows a confirmed route option and a new unconfirmed candidate
-- to coexist for the same trip and transport.

ALTER TABLE trip_route_option
    DROP INDEX uq_trip_route_option_trip_transport;
