import { browser } from '$app/environment';
import { auth, TOKEN_KEY } from '$lib/stores/auth.js';
import { toast } from '$lib/stores/toast.js';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// In-browser mock fallback data for offline / demo preview
const MOCK_STORAGE_KEY = 'ssh_mock_db_v1';

function getMockDb() {
	if (!browser) return null;
	const existing = localStorage.getItem(MOCK_STORAGE_KEY);
	if (existing) {
		try {
			return JSON.parse(existing);
		} catch (e) {
			// reset if corrupted
		}
	}

	const initialDb = {
		tasks: [
			{
				id: 1,
				title: 'E-commerce Product Catalog Digitization',
				description: 'Digitize 150 local artisan products with high-res photos, descriptions, and SKU tagging for Shopify store.',
				category: 'Digital Operations',
				status: 'OPEN',
				budget: 1200.0,
				durationDays: 5,
				isPaid: true,
				smeId: 101,
				smeBusinessName: 'Kasi Craftworks Co.',
				assignedToId: null,
				assignedToName: null,
				createdAt: new Date(Date.now() - 3600000 * 24 * 3).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 24 * 3).toISOString()
			},
			{
				id: 2,
				title: 'Social Media Campaign & Content Creation',
				description: 'Design 12 branded Canva posts and schedule 3 reels promoting our spring collection in Gauteng.',
				category: 'Marketing & Creative',
				status: 'IN_PROGRESS',
				budget: 850.0,
				durationDays: 4,
				isPaid: true,
				smeId: 102,
				smeBusinessName: 'Ubuntu Organic Foods',
				assignedToId: 1,
				assignedToName: 'Thabo Mokoena',
				createdAt: new Date(Date.now() - 3600000 * 24 * 5).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 24 * 1).toISOString()
			},
			{
				id: 3,
				title: 'Financial Records Reconciliation & Invoicing',
				description: 'Reconcile 3 months of bank statements and configure invoice templates in Sage Cloud Accounting.',
				category: 'Accounting & Finance',
				status: 'COMPLETED',
				budget: 1500.0,
				durationDays: 7,
				isPaid: true,
				smeId: 103,
				smeBusinessName: 'Lindiwe Logistics',
				assignedToId: 1,
				assignedToName: 'Thabo Mokoena',
				createdAt: new Date(Date.now() - 3600000 * 24 * 10).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 24 * 2).toISOString()
			},
			{
				id: 4,
				title: 'Customer Onboarding & Verification Calls',
				description: 'Conduct 40 outbound calls to verify delivery addresses and update customer satisfaction notes in CRM.',
				category: 'Customer Support',
				status: 'VERIFIED',
				budget: 700.0,
				durationDays: 3,
				isPaid: true,
				smeId: 101,
				smeBusinessName: 'Kasi Craftworks Co.',
				assignedToId: 1,
				assignedToName: 'Thabo Mokoena',
				createdAt: new Date(Date.now() - 3600000 * 24 * 14).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 24 * 4).toISOString()
			},
			{
				id: 5,
				title: 'POS Hardware Setup & Inventory Audit',
				description: 'Setup Yoco payment terminals at 2 pop-up stalls and conduct physical stock count.',
				category: 'IT & Technical',
				status: 'OPEN',
				budget: 950.0,
				durationDays: 2,
				isPaid: true,
				smeId: 104,
				smeBusinessName: 'Soweto Solar Solutions',
				assignedToId: null,
				assignedToName: null,
				createdAt: new Date(Date.now() - 3600000 * 12).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 12).toISOString()
			}
		],
		experiences: [
			{
				id: 1,
				status: 'VERIFIED',
				isVerified: true,
				feedback: 'Thabo showed exceptional attention to detail and completed customer verification ahead of schedule. Excellent communication skills.',
				rating: 4.8,
				verifiedAt: new Date(Date.now() - 3600000 * 24 * 4).toISOString(),
				taskId: 4,
				taskTitle: 'Customer Onboarding & Verification Calls',
				smeBusinessName: 'Kasi Craftworks Co.',
				youthId: 1,
				youthName: 'Thabo Mokoena',
				createdAt: new Date(Date.now() - 3600000 * 24 * 14).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 24 * 4).toISOString()
			},
			{
				id: 2,
				status: 'PENDING_VERIFICATION',
				isVerified: false,
				feedback: null,
				rating: null,
				verifiedAt: null,
				taskId: 3,
				taskTitle: 'Financial Records Reconciliation & Invoicing',
				smeBusinessName: 'Lindiwe Logistics',
				youthId: 1,
				youthName: 'Thabo Mokoena',
				createdAt: new Date(Date.now() - 3600000 * 24 * 10).toISOString(),
				updatedAt: new Date(Date.now() - 3600000 * 24 * 2).toISOString()
			}
		]
	};

	saveMockDb(initialDb);
	return initialDb;
}

function saveMockDb(db) {
	if (!browser) return;
	localStorage.setItem(MOCK_STORAGE_KEY, JSON.stringify(db));
}

/**
 * Executes a simulated mock response for offline/fallback mode
 */
function handleMockFallback(endpoint, method, body) {
	const db = getMockDb();
	const cleanPath = endpoint.split('?')[0];
	const urlObj = new URL(endpoint, 'http://localhost');
	const params = urlObj.searchParams;

	// AUTH: Register
	if (cleanPath === '/api/v1/auth/register' && method === 'POST') {
		const user = {
			id: Math.floor(Math.random() * 1000) + 10,
			name: body.name,
			email: body.email,
			role: body.role,
			isActive: true,
			createdAt: new Date().toISOString(),
			updatedAt: new Date().toISOString()
		};
		return {
			token: 'mock-jwt-token-' + Date.now(),
			tokenType: 'Bearer',
			expiresIn: 86400000,
			user
		};
	}

	// AUTH: Login
	if (cleanPath === '/api/v1/auth/login' && method === 'POST') {
		let role = 'YOUTH';
		let name = 'Thabo Mokoena';
		const email = body.email || '';
		if (email.includes('sme') || email.includes('business')) {
			role = 'SME';
			name = 'Kasi Craftworks Co.';
		} else if (email.includes('corp') || email.includes('sponsor')) {
			role = 'CORPORATE';
			name = 'Standard Bank Foundation';
		} else if (email.includes('admin')) {
			role = 'ADMIN';
			name = 'Platform Admin';
		}

		const user = {
			id: 1,
			name,
			email: body.email,
			role,
			isActive: true,
			createdAt: new Date().toISOString(),
			updatedAt: new Date().toISOString()
		};
		return {
			token: 'mock-jwt-token-' + Date.now(),
			tokenType: 'Bearer',
			expiresIn: 86400000,
			user
		};
	}

	// TASKS: List
	if (cleanPath === '/api/v1/tasks' && method === 'GET') {
		return db.tasks;
	}

	// TASKS: Get single
	const taskGetMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)$/);
	if (taskGetMatch && method === 'GET') {
		const id = Number(taskGetMatch[1]);
		const found = db.tasks.find((t) => t.id === id);
		if (!found) throw new Error('Task not found');
		return found;
	}

	// TASKS: Create
	if (cleanPath === '/api/v1/tasks' && method === 'POST') {
		const newTask = {
			id: db.tasks.length > 0 ? Math.max(...db.tasks.map((t) => t.id)) + 1 : 1,
			title: body.title,
			description: body.description || '',
			category: body.category,
			status: 'OPEN',
			budget: Number(body.budget) || 0,
			durationDays: Number(body.durationDays) || 7,
			isPaid: Boolean(body.isPaid),
			smeId: 101,
			smeBusinessName: 'Kasi Craftworks Co.',
			assignedToId: null,
			assignedToName: null,
			createdAt: new Date().toISOString(),
			updatedAt: new Date().toISOString()
		};
		db.tasks.unshift(newTask);
		saveMockDb(db);
		return newTask;
	}

	// TASKS: Assign
	const taskAssignMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/assign$/);
	if (taskAssignMatch && method === 'PATCH') {
		const id = Number(taskAssignMatch[1]);
		const youthId = Number(params.get('youthId') || 1);
		const task = db.tasks.find((t) => t.id === id);
		if (!task) throw new Error('Task not found');
		task.assignedToId = youthId;
		task.assignedToName = 'Youth Candidate #' + youthId;
		task.status = 'IN_PROGRESS';
		task.updatedAt = new Date().toISOString();
		saveMockDb(db);
		return task;
	}

	// TASKS: Complete
	const taskCompleteMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/complete$/);
	if (taskCompleteMatch && method === 'PATCH') {
		const id = Number(taskCompleteMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task) throw new Error('Task not found');
		task.status = 'COMPLETED';
		task.updatedAt = new Date().toISOString();

		// Auto create pending experience if not exists
		let exp = db.experiences.find((e) => e.taskId === id);
		if (!exp) {
			exp = {
				id: db.experiences.length + 1,
				status: 'PENDING_VERIFICATION',
				isVerified: false,
				feedback: null,
				rating: null,
				verifiedAt: null,
				taskId: task.id,
				taskTitle: task.title,
				smeBusinessName: task.smeBusinessName || 'SME Partner',
				youthId: task.assignedToId || 1,
				youthName: task.assignedToName || 'Youth Candidate',
				createdAt: new Date().toISOString(),
				updatedAt: new Date().toISOString()
			};
			db.experiences.unshift(exp);
		}
		saveMockDb(db);
		return task;
	}

	// EXPERIENCES: List for youth
	const expYouthMatch = cleanPath.match(/^\/api\/v1\/experiences\/youth\/(\d+)$/);
	if (expYouthMatch && method === 'GET') {
		const youthId = Number(expYouthMatch[1]);
		return db.experiences.filter((e) => Number(e.youthId) === youthId);
	}

	// EXPERIENCES: Get single
	const expGetMatch = cleanPath.match(/^\/api\/v1\/experiences\/(\d+)$/);
	if (expGetMatch && method === 'GET') {
		const id = Number(expGetMatch[1]);
		const exp = db.experiences.find((e) => e.id === id);
		if (!exp) throw new Error('Experience not found');
		return exp;
	}

	// EXPERIENCES: Verify
	const expVerifyMatch = cleanPath.match(/^\/api\/v1\/experiences\/(\d+)\/verify$/);
	if (expVerifyMatch && method === 'PATCH') {
		const id = Number(expVerifyMatch[1]);
		const feedback = params.get('feedback') || '';
		const rating = Number(params.get('rating') || 5);
		const exp = db.experiences.find((e) => e.id === id);
		if (!exp) throw new Error('Experience not found');

		exp.isVerified = true;
		exp.status = 'VERIFIED';
		exp.feedback = feedback;
		exp.rating = rating;
		exp.verifiedAt = new Date().toISOString();
		exp.updatedAt = new Date().toISOString();

		// Also update corresponding task if available
		const task = db.tasks.find((t) => t.id === exp.taskId);
		if (task) {
			task.status = 'VERIFIED';
			task.updatedAt = new Date().toISOString();
		}

		saveMockDb(db);
		return exp;
	}

	throw new Error(`Unhandled mock route: ${method} ${endpoint}`);
}

/**
 * Main HTTP request wrapper
 * @param {string} endpoint
 * @param {RequestInit & { isAuth?: boolean }} options
 */
export async function request(endpoint, options = {}) {
	const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
	const headers = new Headers(options.headers || {});

	if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) {
		headers.set('Content-Type', 'application/json');
	}

	// Add Bearer token for authenticated requests
	if (browser) {
		const token = localStorage.getItem(TOKEN_KEY);
		if (token && !headers.has('Authorization')) {
			headers.set('Authorization', `Bearer ${token}`);
		}
	}

	let response;
	try {
		response = await fetch(url, {
			...options,
			headers
		});
	} catch (networkError) {
		// If backend is not running or network fails, gracefully fallback to mock database in browser
		if (browser) {
			console.warn(`[API] Backend unavailable at ${API_BASE_URL}. Using browser fallback data.`, networkError);
			try {
				const body = options.body && typeof options.body === 'string' ? JSON.parse(options.body) : options.body;
				const mockData = handleMockFallback(endpoint, options.method || 'GET', body);
				return mockData;
			} catch (mockErr) {
				console.error('[API] Fallback handler failed:', mockErr);
				throw new Error(mockErr.message || 'Network connection failed and mock response was unavailable.');
			}
		}
		throw new Error('Could not connect to backend server at ' + API_BASE_URL);
	}

	// Handle 401 Unauthorized
	if (response.status === 401) {
		if (browser && !endpoint.includes('/auth/')) {
			toast.error('Session expired. Please log in again.');
			auth.logout();
		}
		throw new Error('Unauthorized');
	}

	let json;
	try {
		json = await response.json();
	} catch (e) {
		if (!response.ok) {
			throw new Error(`Request failed with status ${response.status} (${response.statusText})`);
		}
		return null;
	}

	if (!response.ok || (json && json.success === false)) {
		const msg = json?.message || `Request failed with status ${response.status}`;
		throw new Error(msg);
	}

	// Envelope: { success: true, message: "...", data: { ... } }
	return json.data !== undefined ? json.data : json;
}

export const api = {
	get: (endpoint, options = {}) => request(endpoint, { ...options, method: 'GET' }),
	post: (endpoint, body, options = {}) =>
		request(endpoint, {
			...options,
			method: 'POST',
			body: body instanceof FormData ? body : JSON.stringify(body)
		}),
	patch: (endpoint, body, options = {}) =>
		request(endpoint, {
			...options,
			method: 'PATCH',
			body: body ? (body instanceof FormData ? body : JSON.stringify(body)) : undefined
		}),
	delete: (endpoint, options = {}) => request(endpoint, { ...options, method: 'DELETE' }),

	// Domain helpers
	auth: {
		login: (credentials) => api.post('/api/v1/auth/login', credentials),
		register: (userData) => api.post('/api/v1/auth/register', userData)
	},
	tasks: {
		list: () => api.get('/api/v1/tasks'),
		get: (id) => api.get(`/api/v1/tasks/${id}`),
		create: (task) => api.post('/api/v1/tasks', task),
		assign: (id, youthId) => api.patch(`/api/v1/tasks/${id}/assign?youthId=${encodeURIComponent(youthId)}`),
		complete: (id) => api.patch(`/api/v1/tasks/${id}/complete`)
	},
	experiences: {
		get: (id) => api.get(`/api/v1/experiences/${id}`),
		listForYouth: (youthId) => api.get(`/api/v1/experiences/youth/${youthId}`),
		verify: (id, feedback = '', rating = 5) => {
			const query = new URLSearchParams({
				feedback: feedback || '',
				rating: String(rating)
			});
			return api.patch(`/api/v1/experiences/${id}/verify?${query.toString()}`);
		}
	}
};

export default api;
