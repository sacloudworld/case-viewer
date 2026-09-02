import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 5 },
        { duration: '30s', target: 25 },
        { duration: '30s', target: 50 },
        { duration: '3m', target: 50 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {

    const loginPayload = JSON.stringify({
        username: __ENV.USERNAME || 'sachin',
        password: __ENV.PASSWORD || 'password123'
    });

    const loginParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(
        `${BASE_URL}/api/auth/login`,
        loginPayload,
        loginParams
    );

    check(response, {
        'login successful': (r) => r.status === 200,
        'token exists': (r) => r.json('token') !== undefined,
    });

    if (response.status !== 200) {
        throw new Error(
            `Login failed. Status: ${response.status}, Body: ${response.body}`
        );
    }

    return {
        token: response.json('token'),
    };
}


export default function (data) {

    const timestamp = Date.now();
    const vu = __VU;
    const iteration = __ITER;

    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
            'Content-Type': 'application/json',
        },
    };


    // ==================================================
    // 1. CREATE USER
    // ==================================================

    const createUserPayload = JSON.stringify({
        username: `testuser_${vu}_${iteration}_${timestamp}`,
        password: 'Password@123',
        email: `testuser_${vu}_${iteration}_${timestamp}@example.com`
       
    });

    const createUserResponse = http.post(
        `${BASE_URL}/api/users`,
        createUserPayload,
        params
    );

    check(createUserResponse, {
        'create user status is 200/201': (r) =>
            r.status === 200 || r.status === 201,

        'create user response exists': (r) =>
            r.body && r.body.length > 0,
    });


    let userId = null;

    if (
        createUserResponse.status === 200 ||
        createUserResponse.status === 201
    ) {
        try {
            userId = createUserResponse.json('id');
        } catch (e) {
            console.log('Unable to read user ID');
        }
    }


    // ==================================================
    // 2. GET USER
    // ==================================================

    if (userId) {

        const getUserResponse = http.get(
            `${BASE_URL}/api/users/${userId}`,
            params
        );

        check(getUserResponse, {
            'get user status is 200': (r) =>
                r.status === 200,

            'get user contains id': (r) =>
                r.json('id') !== undefined,

            'get user contains username': (r) =>
                r.json('username') !== undefined,
        });
    }


    // ==================================================
    // 3. CREATE CASE
    // ==================================================

    const caseNumber =
        `CASE-${vu}-${iteration}-${timestamp}`;

    const createCasePayload = JSON.stringify({
        caseNumber: caseNumber,
        title: `Test Case ${caseNumber}`,
        description: 'Case created during performance testing',
        status: 'OPEN'
    });

    const createCaseResponse = http.post(
        `${BASE_URL}/api/cases`,
        createCasePayload,
        params
    );

    check(createCaseResponse, {
        'create case status is 200/201': (r) =>
            r.status === 200 || r.status === 201,

        'create case response exists': (r) =>
            r.body && r.body.length > 0,
    });


    // ==================================================
    // 4. GET CASE
    // ==================================================

    const getCaseResponse = http.get(
        `${BASE_URL}/api/cases/${caseNumber}`,
        params
    );

    check(getCaseResponse, {
        'get case status is 200': (r) =>
            r.status === 200,

        'case contains caseNumber': (r) =>
            r.json('caseNumber') !== undefined,

        'case contains activities': (r) =>
            Array.isArray(r.json('activities')),
    });


    // ==================================================
    // 5. CREATE SECOND USER
    // ==================================================

    const secondTimestamp = Date.now();

    const secondUserPayload = JSON.stringify({
        username: `seconduser_${vu}_${iteration}_${secondTimestamp}`,
        password: 'Password@123',
        email: `seconduser_${vu}_${iteration}_${secondTimestamp}@example.com`
        
    });

    const secondCreateUserResponse = http.post(
        `${BASE_URL}/api/users`,
        secondUserPayload,
        params
    );

    check(secondCreateUserResponse, {
        'second create user status is 200/201': (r) =>
            r.status === 200 || r.status === 201,

        'second user response exists': (r) =>
            r.body && r.body.length > 0,
    });


    // ==================================================
    // WAIT
    // ==================================================

    sleep(1);
}