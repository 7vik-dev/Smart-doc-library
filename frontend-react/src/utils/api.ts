import axios from 'axios';

const API_BASE = '/api';
const ACTUATOR_BASE = '/actuator';

export const api = axios.create({
  baseURL: API_BASE,
});

export const actuator = axios.create({
  baseURL: ACTUATOR_BASE,
});
