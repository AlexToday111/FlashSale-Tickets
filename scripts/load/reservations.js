import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    reserve_burst: {
      executor: 'constant-vus',
      vus: 100,
      duration: '30s'
    }
  }
};

const INVENTORY_URL = __ENV.INVENTORY_URL || 'http://localhost:8082';
const EVENT_ID = __ENV.EVENT_ID || '00000000-0000-0000-0000-000000000002';

export default function () {
  const userSuffix = `${__VU}-${__ITER}`.padStart(12, '0').slice(-12);
  const seatSuffix = `${__VU}`.padStart(12, '0').slice(-12);

  const payload = JSON.stringify({
    userId: `00000000-0000-0000-0000-${userSuffix}`,
    eventId: EVENT_ID,
    seats: [`00000000-0000-0000-0000-${seatSuffix}`]
  });

  const response = http.post(`${INVENTORY_URL}/reservation`, payload, {
    headers: {
      'Content-Type': 'application/json',
      'X-Correlation-Id': `demo-${__VU}-${__ITER}`
    }
  });

  check(response, {
    'reservation created or conflict': (r) => r.status === 200 || r.status === 201 || r.status === 409
  });

  sleep(0.2);
}
