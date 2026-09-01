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
const CASE_NUMBER = __ENV.CASE_NUMBER || 'CASE-100001';

export function setup() {
    const loginPayload = JSON.stringify({
        username: __ENV.USERNAME || 'user41',
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
        throw new Error(`Login failed. Status: ${response.status}, Body: ${response.body}`);
    }

    return {
        token: response.json('token'),
    };
}

export default function (data) {
    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
        },
    };

    const response = http.get(
        `${BASE_URL}/api/cases/${CASE_NUMBER}`,
        params
    );

    check(response, {
        'case view status is 200': (r) => r.status === 200,
        'response contains caseNumber': (r) => r.json('caseNumber') !== undefined,
        'response contains activities': (r) => Array.isArray(r.json('activities')),
    });

    sleep(1);
}
