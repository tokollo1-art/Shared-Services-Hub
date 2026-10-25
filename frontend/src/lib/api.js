
import { browser } from '$app/environment';
import { auth, TOKEN_KEY } from '$lib/stores/auth.js';
import { toast } from '$lib/stores/toast.js';

const API_BASE_URL =
	import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// In-browser mock fallback data for offline / demo preview
const MOCK_STORAGE_KEY = 'ssh_mock_db_v2';

// Separate storage for user profiles
const PROFILE_STORAGE_KEY = 'ssh_profiles_v1';

/* =========================================
   PROFILE STORAGE HELPERS
========================================= */

// Get the current user from the auth store when needed.
// This supports Svelte stores and avoids requiring a backend.
function getCurrentUser() {
	if (!browser) return null;

	let currentUser = null;

	const unsubscribe = auth.subscribe((value) => {
		currentUser = value?.user ?? value ?? null;
	});

	unsubscribe();

	return currentUser;
}

function getProfileUserKey(user) {
	const email = user?.email?.trim().toLowerCase();

	if (email) {
		return `email:${email}`;
	}

	if (user?.id != null) {
		return `id:${user.id}`;
	}

	throw new Error(
		'Cannot identify the logged-in user. Please log in again.'
	);
}

function readSavedProfiles() {
	if (!browser) {
		throw new Error('Browser storage is unavailable.');
	}

	try {
		const saved = localStorage.getItem(PROFILE_STORAGE_KEY);
		const profiles = saved ? JSON.parse(saved) : {};

		if (
			!profiles ||
			typeof profiles !== 'object' ||
			Array.isArray(profiles)
		) {
			throw new Error('Invalid profile storage format.');
		}

		return profiles;
	} catch {
		throw new Error(
			'Could not read saved profiles from browser storage.'
		);
	}
}

function getLocalProfile(user) {
	const resolvedUser = user ?? getCurrentUser();
	const key = getProfileUserKey(resolvedUser);
	const profiles = readSavedProfiles();

	if (!profiles[key]) {
		return {
			hasExistingProfile: false
		};
	}

	return {
		...profiles[key],
		hasExistingProfile: true
	};
}

function saveLocalProfile(profileData, user) {
	if (!browser) {
		throw new Error('Browser storage is unavailable.');
	}

	const resolvedUser = user ?? getCurrentUser();
	const key = getProfileUserKey(resolvedUser);
	const profiles = readSavedProfiles();
	const previousProfile = profiles[key] || {};

	const savedProfile = {
		...previousProfile,
		...profileData,
		profileId: previousProfile.profileId || key,
		hasExistingProfile: true,
		updatedAt: new Date().toISOString()
	};

	try {
		profiles[key] = savedProfile;

		localStorage.setItem(
			PROFILE_STORAGE_KEY,
			JSON.stringify(profiles)
		);
	} catch {
		throw new Error(
			'Could not save the profile. Check browser storage availability.'
		);
	}

	return savedProfile;
}

/* =========================================
   MOCK DATABASE
========================================= */

function getMockDb() {
	if (!browser) return null;

	const existing = localStorage.getItem(MOCK_STORAGE_KEY);

	if (existing) {
		try {
			return JSON.parse(existing);
		} catch (e) {
			// Reset if corrupted
		}
	}

	const initialDb = {
		tasks: [
			{
				id: 1,
				title: 'E-commerce Product Catalog Digitization',
				description:
					'Digitize 150 local artisan products with high-res photos, descriptions, and SKU tagging for Shopify store.',
				category: 'Digital Operations',
				status: 'OPEN',
				budget: 1200.0,
				durationDays: 5,
				isPaid: true,
				smeId: 101,
				smeBusinessName: 'Kasi Craftworks Co.',
				assignedToId: null,
				assignedToName: null,
				createdAt: new Date(
					Date.now() - 3600000 * 24 * 3
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 24 * 3
				).toISOString()
			},
			{
				id: 2,
				title: 'Social Media Campaign & Content Creation',
				description:
					'Design 12 branded Canva posts and schedule 3 reels promoting our spring collection in Gauteng.',
				category: 'Marketing & Creative',
				status: 'IN_PROGRESS',
				budget: 850.0,
				durationDays: 4,
				isPaid: true,
				smeId: 102,
				smeBusinessName: 'Ubuntu Organic Foods',
				assignedToId: 1,
				assignedToName: 'Thabo Mokoena',
				createdAt: new Date(
					Date.now() - 3600000 * 24 * 5
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 24
				).toISOString()
			},
			{
				id: 3,
				title: 'Financial Records Reconciliation & Invoicing',
				description:
					'Reconcile 3 months of bank statements and configure invoice templates in Sage Cloud Accounting.',
				category: 'Accounting & Finance',
				status: 'COMPLETED',
				budget: 1500.0,
				durationDays: 7,
				isPaid: true,
				smeId: 103,
				smeBusinessName: 'Lindiwe Logistics',
				assignedToId: 1,
				assignedToName: 'Thabo Mokoena',
				createdAt: new Date(
					Date.now() - 3600000 * 24 * 10
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 24 * 2
				).toISOString()
			},
			{
				id: 4,
				title: 'Customer Onboarding & Verification Calls',
				description:
					'Conduct 40 outbound calls to verify delivery addresses and update customer satisfaction notes in CRM.',
				category: 'Customer Support',
				status: 'VERIFIED',
				budget: 700.0,
				durationDays: 3,
				isPaid: true,
				smeId: 101,
				smeBusinessName: 'Kasi Craftworks Co.',
				assignedToId: 1,
				assignedToName: 'Thabo Mokoena',
				createdAt: new Date(
					Date.now() - 3600000 * 24 * 14
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 24 * 4
				).toISOString()
			},
			{
				id: 5,
				title: 'POS Hardware Setup & Inventory Audit',
				description:
					'Setup Yoco payment terminals at 2 pop-up stalls and conduct physical stock count.',
				category: 'IT & Technical',
				status: 'OPEN',
				budget: 950.0,
				durationDays: 2,
				isPaid: true,
				smeId: 104,
				smeBusinessName: 'Soweto Solar Solutions',
				assignedToId: null,
				assignedToName: null,
				createdAt: new Date(
					Date.now() - 3600000 * 12
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 12
				).toISOString()
			}
		],
		experiences: [
			{
				id: 1,
				status: 'VERIFIED',
				isVerified: true,
				feedback:
					'Thabo showed exceptional attention to detail and completed customer verification ahead of schedule. Excellent communication skills.',
				rating: 4.8,
				reference: 'CRM export: customer-verification-batch-04.csv',
				verifiedAt: new Date(
					Date.now() - 3600000 * 24 * 4
				).toISOString(),
				taskId: 4,
				taskTitle: 'Customer Onboarding & Verification Calls',
				smeBusinessName: 'Kasi Craftworks Co.',
				youthId: 1,
				youthName: 'Thabo Mokoena',
				createdAt: new Date(
					Date.now() - 3600000 * 24 * 14
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 24 * 4
				).toISOString()
			},
			{
				id: 2,
				status: 'PENDING',
				isVerified: false,
				feedback: null,
				rating: null,
				reference: null,
				verifiedAt: null,
				taskId: 3,
				taskTitle:
					'Financial Records Reconciliation & Invoicing',
				smeBusinessName: 'Lindiwe Logistics',
				youthId: 1,
				youthName: 'Thabo Mokoena',
				createdAt: new Date(
					Date.now() - 3600000 * 24 * 10
				).toISOString(),
				updatedAt: new Date(
					Date.now() - 3600000 * 24 * 2
				).toISOString()
			}
		],
		applications: [],
		payments: []
	};

	saveMockDb(initialDb);
	return initialDb;
}

function saveMockDb(db) {
	if (!browser) return;

	localStorage.setItem(
		MOCK_STORAGE_KEY,
		JSON.stringify(db)
	);
}

/* =========================================
   MOCK API FALLBACK
========================================= */

function handleMockFallback(endpoint, method, body) {
	const db = getMockDb();
	const cleanPath = endpoint.split('?')[0];
	const urlObj = new URL(endpoint, 'http://localhost');
	const params = urlObj.searchParams;


	if (
		cleanPath === '/api/v1/auth/register' &&
		method === 'POST'
	) {
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

	if (
		cleanPath === '/api/v1/auth/login' &&
		method === 'POST'
	) {
		let role = 'YOUTH';
		let name = 'Thabo Mokoena';
		const email = body.email || '';

		if (
			email.includes('sme') ||
			email.includes('business')
		) {
			role = 'SME';
			name = 'Kasi Craftworks Co.';
		} else if (
			email.includes('corp') ||
			email.includes('sponsor')
		) {
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

	if (
		cleanPath === '/api/v1/tasks' &&
		method === 'GET'
	) {
		return db.tasks;
	}

	// TASKS: My posted tasks (SME)
	if (cleanPath === '/api/v1/tasks/mine' && method === 'GET') {
		return db.tasks.filter((t) => Number(t.smeId) === 101);
	}

	// TASKS: My posted tasks (SME)
	if (cleanPath === '/api/v1/tasks/mine' && method === 'GET') {
		return db.tasks.filter((t) => Number(t.smeId) === 101);
	}

	// TASKS: Get single
	const taskGetMatch = cleanPath.match(
		/^\/api\/v1\/tasks\/(\d+)$/
	);

	if (taskGetMatch && method === 'GET') {
		const id = Number(taskGetMatch[1]);
		const found = db.tasks.find((t) => t.id === id);

		if (!found) throw new Error('Task not found');

		return found;
	}


	if (
		cleanPath === '/api/v1/tasks' &&
		method === 'POST'
	) {
		const newTask = {
			id:
				db.tasks.length > 0
					? Math.max(...db.tasks.map((t) => t.id)) + 1
					: 1,
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


	const taskAssignMatch = cleanPath.match(
		/^\/api\/v1\/tasks\/(\d+)\/assign$/
	);

	if (taskAssignMatch && method === 'PATCH') {
		const id = Number(taskAssignMatch[1]);
		const youthId = Number(params.get('youthId') || 1);
		const task = db.tasks.find((t) => t.id === id);

		if (!task) throw new Error('Task not found');

		const application = db.applications.find(
			(a) => a.taskId === id && Number(a.youthId) === youthId && a.status === 'PENDING'
		);
		if (!application) throw new Error('Youth has not applied for this task');
		db.applications.forEach((a) => {
			if (a.taskId === id && a.status === 'PENDING') {
				a.status = Number(a.youthId) === youthId ? 'ACCEPTED' : 'REJECTED';
				a.decidedAt = new Date().toISOString();
			}
		});
		task.assignedToId = youthId;
		task.assignedToName = application.youthName || 'Youth Candidate #' + youthId;
		task.status = 'IN_PROGRESS';
		task.updatedAt = new Date().toISOString();

		saveMockDb(db);
		return task;
	}

	const applyMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/apply$/);
	if (applyMatch && method === 'POST') {
		const id = Number(applyMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task || task.status !== 'OPEN') throw new Error('Task is not open for applications');
		const youthId = 1;
		let application = db.applications.find(
			(a) => a.taskId === id && Number(a.youthId) === youthId
		);
		if (application && ['PENDING', 'ACCEPTED'].includes(application.status)) {
			throw new Error('An application for this task is already pending');
		}
		const verified = db.experiences.filter(
			(e) => Number(e.youthId) === youthId && e.isVerified
		).length;
		const limits = [1, 2, 3, Infinity];
		const tierIndex = verified >= 5 ? 3 : verified >= 3 ? 2 : verified >= 1 ? 1 : 0;
		const active =
			db.applications.filter(
				(a) => Number(a.youthId) === youthId && a.status === 'PENDING'
			).length +
			db.tasks.filter(
				(t) => t.status === 'IN_PROGRESS' && Number(t.assignedToId) === youthId
			).length;
		if (active >= limits[tierIndex]) {
			throw new Error(
				`Tier TIER_${tierIndex} allows at most ${limits[tierIndex]} active reservation(s)`
			);
		}
		const expiresAt = new Date(Date.now() + 48 * 3600 * 1000).toISOString();
		if (!application) {
			application = {
				id: db.applications.length + 1,
				taskId: id,
				taskTitle: task.title,
				youthId,
				youthName: 'Thabo Mokoena',
				status: 'PENDING',
				coverNote: body?.coverNote || '',
				appliedAt: new Date().toISOString(),
				expiresAt,
				decidedAt: null
			};
			db.applications.push(application);
		} else {
			application.status = 'PENDING';
			application.coverNote = body?.coverNote || '';
			application.expiresAt = expiresAt;
			application.decidedAt = null;
		}
		saveMockDb(db);
		return application;
	}

	// APPLICATIONS: Mine
	if (cleanPath === '/api/v1/applications/mine' && method === 'GET') {
		return db.applications.filter((a) => Number(a.youthId) === 1);
	}

	// APPLICATIONS: For a task (SME)
	const taskApplicationsMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/applications$/);
	if (taskApplicationsMatch && method === 'GET') {
		const id = Number(taskApplicationsMatch[1]);
		return db.applications.filter((a) => a.taskId === id);
	}

	// APPLICATIONS: Withdraw
	const withdrawMatch = cleanPath.match(/^\/api\/v1\/applications\/(\d+)\/withdraw$/);
	if (withdrawMatch && method === 'PATCH') {
		const id = Number(withdrawMatch[1]);
		const application = db.applications.find((a) => a.id === id);
		if (!application) throw new Error('Application not found');
		application.status = 'WITHDRAWN';
		application.decidedAt = new Date().toISOString();
		saveMockDb(db);
		return application;
	}

	// TIERS: Mine
	if (cleanPath === '/api/v1/tiers/me' && method === 'GET') {
		const verified = db.experiences.filter(
			(e) => Number(e.youthId) === 1 && e.isVerified
		).length;
		const tierIndex = verified >= 5 ? 3 : verified >= 3 ? 2 : verified >= 1 ? 1 : 0;
		const limits = [1, 2, 3, 2147483647];
		const active =
			db.applications.filter((a) => Number(a.youthId) === 1 && a.status === 'PENDING')
				.length +
			db.tasks.filter(
				(t) => t.status === 'IN_PROGRESS' && Number(t.assignedToId) === 1
			).length;
		return {
			tier: `TIER_${tierIndex}`,
			verifiedExperienceCount: verified,
			minVerifiedExperiences: [0, 1, 3, 5][tierIndex],
			maxVerifiedExperiences: [0, 2, 4, 2147483647][tierIndex],
			activeReservations: active,
			maxConcurrentReservations: limits[tierIndex],
			reservationWindowHours: 48,
			canApply: active < limits[tierIndex],
			checkedAt: new Date().toISOString()
		};
	}

	// YOUTH: My profile
	if (cleanPath === '/api/v1/youth/me' && method === 'GET') {
		return {
			id: 1,
			userId: 1,
			userName: 'Thabo Mokoena',
			location: 'Cape Town',
			availability: 'Immediate',
			isAlumni: false
		};
	}

	// APPLICATIONS: Apply
	const applyMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/apply$/);
	if (applyMatch && method === 'POST') {
		const id = Number(applyMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task || task.status !== 'OPEN') throw new Error('Task is not open for applications');
		const youthId = 1;
		let application = db.applications.find(
			(a) => a.taskId === id && Number(a.youthId) === youthId
		);
		if (application && ['PENDING', 'ACCEPTED'].includes(application.status)) {
			throw new Error('An application for this task is already pending');
		}
		const verified = db.experiences.filter(
			(e) => Number(e.youthId) === youthId && e.isVerified
		).length;
		const limits = [1, 2, 3, Infinity];
		const tierIndex = verified >= 5 ? 3 : verified >= 3 ? 2 : verified >= 1 ? 1 : 0;
		const active =
			db.applications.filter(
				(a) => Number(a.youthId) === youthId && a.status === 'PENDING'
			).length +
			db.tasks.filter(
				(t) => t.status === 'IN_PROGRESS' && Number(t.assignedToId) === youthId
			).length;
		if (active >= limits[tierIndex]) {
			throw new Error(
				`Tier TIER_${tierIndex} allows at most ${limits[tierIndex]} active reservation(s)`
			);
		}
		const expiresAt = new Date(Date.now() + 48 * 3600 * 1000).toISOString();
		if (!application) {
			application = {
				id: db.applications.length + 1,
				taskId: id,
				taskTitle: task.title,
				youthId,
				youthName: 'Thabo Mokoena',
				status: 'PENDING',
				coverNote: body?.coverNote || '',
				appliedAt: new Date().toISOString(),
				expiresAt,
				decidedAt: null
			};
			db.applications.push(application);
		} else {
			application.status = 'PENDING';
			application.coverNote = body?.coverNote || '';
			application.expiresAt = expiresAt;
			application.decidedAt = null;
		}
		saveMockDb(db);
		return application;
	}

	// APPLICATIONS: Mine
	if (cleanPath === '/api/v1/applications/mine' && method === 'GET') {
		return db.applications.filter((a) => Number(a.youthId) === 1);
	}

	// APPLICATIONS: For a task (SME)
	const taskApplicationsMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/applications$/);
	if (taskApplicationsMatch && method === 'GET') {
		const id = Number(taskApplicationsMatch[1]);
		return db.applications.filter((a) => a.taskId === id);
	}

	// APPLICATIONS: Withdraw
	const withdrawMatch = cleanPath.match(/^\/api\/v1\/applications\/(\d+)\/withdraw$/);
	if (withdrawMatch && method === 'PATCH') {
		const id = Number(withdrawMatch[1]);
		const application = db.applications.find((a) => a.id === id);
		if (!application) throw new Error('Application not found');
		application.status = 'WITHDRAWN';
		application.decidedAt = new Date().toISOString();
		saveMockDb(db);
		return application;
	}

	// TIERS: Mine
	if (cleanPath === '/api/v1/tiers/me' && method === 'GET') {
		const verified = db.experiences.filter(
			(e) => Number(e.youthId) === 1 && e.isVerified
		).length;
		const tierIndex = verified >= 5 ? 3 : verified >= 3 ? 2 : verified >= 1 ? 1 : 0;
		const limits = [1, 2, 3, 2147483647];
		const active =
			db.applications.filter((a) => Number(a.youthId) === 1 && a.status === 'PENDING')
				.length +
			db.tasks.filter(
				(t) => t.status === 'IN_PROGRESS' && Number(t.assignedToId) === 1
			).length;
		return {
			tier: `TIER_${tierIndex}`,
			verifiedExperienceCount: verified,
			minVerifiedExperiences: [0, 1, 3, 5][tierIndex],
			maxVerifiedExperiences: [0, 2, 4, 2147483647][tierIndex],
			activeReservations: active,
			maxConcurrentReservations: limits[tierIndex],
			reservationWindowHours: 48,
			canApply: active < limits[tierIndex],
			checkedAt: new Date().toISOString()
		};
	}

	// YOUTH: My profile
	if (cleanPath === '/api/v1/youth/me' && method === 'GET') {
		return {
			id: 1,
			userId: 1,
			userName: 'Thabo Mokoena',
			location: 'Cape Town',
			availability: 'Immediate',
			isAlumni: false
		};
	}


	const taskCompleteMatch = cleanPath.match(
		/^\/api\/v1\/tasks\/(\d+)\/complete$/
	);

	if (taskCompleteMatch && method === 'PATCH') {
		const id = Number(taskCompleteMatch[1]);
		const task = db.tasks.find((t) => t.id === id);

		if (!task) throw new Error('Task not found');

		task.status = 'COMPLETED';
		task.updatedAt = new Date().toISOString();


		let exp = db.experiences.find((e) => e.taskId === id);

		if (!exp) {
			exp = {
				id: db.experiences.length + 1,
				status: 'PENDING',
				isVerified: false,
				feedback: null,
				rating: null,
				reference: null,
				verifiedAt: null,
				taskId: task.id,
				taskTitle: task.title,
				smeBusinessName:
					task.smeBusinessName || 'SME Partner',
				youthId: task.assignedToId || 1,
				youthName:
					task.assignedToName || 'Youth Candidate',
				createdAt: new Date().toISOString(),
				updatedAt: new Date().toISOString()
			};

			db.experiences.unshift(exp);
		} else {
			exp.status = 'PENDING';
			exp.isVerified = false;
			exp.verifiedAt = null;
		}

		saveMockDb(db);
		return task;
	}

	// TASKS: Verify (rating + feedback + reference in the body)
	const taskVerifyMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/verify$/);
	if (taskVerifyMatch && method === 'PATCH') {
		const id = Number(taskVerifyMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task) throw new Error('Task not found');
		if (task.status !== 'COMPLETED') throw new Error('Only a COMPLETED task can be verified');
		if (!body?.feedback || !body?.reference || !body?.rating) {
			throw new Error('Rating, feedback and reference are required');
		}

		task.status = 'VERIFIED';
		task.updatedAt = new Date().toISOString();

		let exp = db.experiences.find((e) => e.taskId === id);
		if (!exp) {
			exp = {
				id: db.experiences.length + 1,
				taskId: task.id,
				taskTitle: task.title,
				smeBusinessName: task.smeBusinessName || 'SME Partner',
				youthId: task.assignedToId || 1,
				youthName: task.assignedToName || 'Youth Candidate',
				createdAt: new Date().toISOString()
			};
			db.experiences.unshift(exp);
		} else {
			exp.status = 'PENDING';
			exp.isVerified = false;
			exp.verifiedAt = null;
		}
		saveMockDb(db);
		return task;
	}

	// TASKS: Verify (rating + feedback + reference in the body)
	const taskVerifyMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/verify$/);
	if (taskVerifyMatch && method === 'PATCH') {
		const id = Number(taskVerifyMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task) throw new Error('Task not found');
		if (task.status !== 'COMPLETED') throw new Error('Only a COMPLETED task can be verified');
		if (!body?.feedback || !body?.reference || !body?.rating) {
			throw new Error('Rating, feedback and reference are required');
		}

		task.status = 'VERIFIED';
		task.updatedAt = new Date().toISOString();

		let exp = db.experiences.find((e) => e.taskId === id);
		if (!exp) {
			exp = {
				id: db.experiences.length + 1,
				taskId: task.id,
				taskTitle: task.title,
				smeBusinessName: task.smeBusinessName || 'SME Partner',
				youthId: task.assignedToId || 1,
				youthName: task.assignedToName || 'Youth Candidate',
				createdAt: new Date().toISOString()
			};
			db.experiences.unshift(exp);
		}
		exp.status = 'VERIFIED';
		exp.isVerified = true;
		exp.feedback = body.feedback;
		exp.rating = Number(body.rating);
		exp.reference = body.reference;
		exp.verifiedAt = new Date().toISOString();
		exp.updatedAt = new Date().toISOString();

		let payment = db.payments.find((p) => p.taskId === id);
		if (!payment) {
			const total = Number(task.budget) || 0;
			const platformFee = Math.round(total * 0.1 * 100) / 100;
			payment = {
				id: db.payments.length + 1,
				taskId: id,
				taskTitle: task.title,
				youthId: exp.youthId,
				youthName: exp.youthName,
				totalAmount: total,
				platformFee,
				youthAmount: Math.round((total - platformFee) * 100) / 100,
				status: 'PENDING',
				reference: null,
				paidAt: null,
				createdAt: new Date().toISOString()
			};
			db.payments.push(payment);
		}
		saveMockDb(db);
		return { task, experience: exp, payment };
	}

	// TASKS: Send back for revision
	const taskRejectMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/reject$/);
	if (taskRejectMatch && method === 'PATCH') {
		const id = Number(taskRejectMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task) throw new Error('Task not found');
		if (task.status !== 'COMPLETED') throw new Error('Only a COMPLETED task can be revised');
		if (!body?.reason) throw new Error('A revision reason is required');
		task.status = 'IN_PROGRESS';
		task.revisionReason = body.reason;
		task.revisionCount = (task.revisionCount || 0) + 1;
		task.updatedAt = new Date().toISOString();
		const exp = db.experiences.find((e) => e.taskId === id);
		if (exp) {
			exp.status = 'REJECTED';
			exp.rejectionReason = body.reason;
			exp.updatedAt = new Date().toISOString();
		}
		exp.status = 'VERIFIED';
		exp.isVerified = true;
		exp.feedback = body.feedback;
		exp.rating = Number(body.rating);
		exp.reference = body.reference;
		exp.verifiedAt = new Date().toISOString();
		exp.updatedAt = new Date().toISOString();

		let payment = db.payments.find((p) => p.taskId === id);
		if (!payment) {
			const total = Number(task.budget) || 0;
			const platformFee = Math.round(total * 0.1 * 100) / 100;
			payment = {
				id: db.payments.length + 1,
				taskId: id,
				taskTitle: task.title,
				youthId: exp.youthId,
				youthName: exp.youthName,
				totalAmount: total,
				platformFee,
				youthAmount: Math.round((total - platformFee) * 100) / 100,
				status: 'PENDING',
				reference: null,
				paidAt: null,
				createdAt: new Date().toISOString()
			};
			db.payments.push(payment);
		}
		saveMockDb(db);
		return { task, experience: exp, payment };
	}

	// TASKS: Send back for revision
	const taskRejectMatch = cleanPath.match(/^\/api\/v1\/tasks\/(\d+)\/reject$/);
	if (taskRejectMatch && method === 'PATCH') {
		const id = Number(taskRejectMatch[1]);
		const task = db.tasks.find((t) => t.id === id);
		if (!task) throw new Error('Task not found');
		if (task.status !== 'COMPLETED') throw new Error('Only a COMPLETED task can be revised');
		if (!body?.reason) throw new Error('A revision reason is required');
		task.status = 'IN_PROGRESS';
		task.revisionReason = body.reason;
		task.revisionCount = (task.revisionCount || 0) + 1;
		task.updatedAt = new Date().toISOString();
		const exp = db.experiences.find((e) => e.taskId === id);
		if (exp) {
			exp.status = 'REJECTED';
			exp.rejectionReason = body.reason;
			exp.updatedAt = new Date().toISOString();
		}

		saveMockDb(db);
		return task;
	}

	
	const expYouthMatch = cleanPath.match(
		/^\/api\/v1\/experiences\/youth\/(\d+)$/
	);

	if (expYouthMatch && method === 'GET') {
		const youthId = Number(expYouthMatch[1]);

		return db.experiences.filter(
			(e) => Number(e.youthId) === youthId
		);
	}


	const expGetMatch = cleanPath.match(
		/^\/api\/v1\/experiences\/(\d+)$/
	);

	if (expGetMatch && method === 'GET') {
		const id = Number(expGetMatch[1]);
		const exp = db.experiences.find((e) => e.id === id);

		if (!exp) throw new Error('Experience not found');

		return exp;
	}

	const expVerifyMatch = cleanPath.match(
		/^\/api\/v1\/experiences\/(\d+)\/verify$/
	);

	if (expVerifyMatch && method === 'PATCH') {
		const id = Number(expVerifyMatch[1]);
		const feedback = body?.feedback || '';
		const rating = Number(body?.rating || 5);
		const reference = body?.reference || '';
		const exp = db.experiences.find((e) => e.id === id);

		if (!exp) throw new Error('Experience not found');
		if (!feedback || !reference) throw new Error('Feedback and reference are required');

		exp.isVerified = true;
		exp.status = 'VERIFIED';
		exp.feedback = feedback;
		exp.rating = rating;
		exp.reference = reference;
		exp.verifiedAt = new Date().toISOString();
		exp.updatedAt = new Date().toISOString();

		
		const task = db.tasks.find((t) => t.id === exp.taskId);

		if (task) {
			task.status = 'VERIFIED';
			task.updatedAt = new Date().toISOString();
		}

		saveMockDb(db);
		return exp;
	}

	// EXPERIENCES: My payments
	if (cleanPath === '/api/v1/payments/mine' && method === 'GET') {
		return db.payments.filter((p) => Number(p.youthId) === 1);
	}

	// PAYMENTS: Payout (sandbox)
	const payoutMatch = cleanPath.match(/^\/api\/v1\/payments\/(\d+)\/payout$/);
	if (payoutMatch && method === 'POST') {
		const id = Number(payoutMatch[1]);
		const payment = db.payments.find((p) => p.id === id);
		if (!payment) throw new Error('Payment not found');
		if (payment.status === 'PAID') throw new Error('Payment has already been paid');
		payment.status = 'PAID';
		payment.reference = 'SANDBOX-' + Date.now();
		payment.paidAt = new Date().toISOString();
		saveMockDb(db);
		return payment;
	}

	// LEDGER: Public entries
	if (cleanPath === '/api/v1/ledger/entries' && method === 'GET') {
		return db.experiences
			.filter((e) => e.isVerified)
			.map((e, index) => ({
				id: index + 1,
				sequence: index + 1,
				experienceId: e.id,
				taskId: e.taskId,
				youthId: e.youthId,
				rating: e.rating,
				feedback: e.feedback,
				reference: e.reference,
				verifiedAt: e.verifiedAt,
				previousHash: index === 0 ? 'GENESIS' : 'demo-hash-' + index,
				hash: 'demo-hash-' + (index + 1)
			}));
	}

	// LEDGER: Verify chain
	if (cleanPath === '/api/v1/ledger/verify' && method === 'GET') {
		return {
			valid: true,
			checkedEntries: db.experiences.filter((e) => e.isVerified).length,
			brokenAtSequence: null,
			message: 'Ledger chain is intact'
		};
	}

	throw new Error(
		`Unhandled mock route: ${method} ${endpoint}`);
}

/* =========================================
   MAIN HTTP REQUEST WRAPPER
========================================= */

export async function request(endpoint, options = {}) {
	const url = endpoint.startsWith('http')
		? endpoint
		: `${API_BASE_URL}${endpoint}`;

	const headers = new Headers(options.headers || {});

	if (
		!headers.has('Content-Type') &&
		options.body &&
		!(options.body instanceof FormData)
	) {
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
		if (browser) {
			console.warn(
				`[API] Backend unavailable at ${API_BASE_URL}. Using browser fallback data.`,
				networkError
			);

			try {
				const body =
					options.body && typeof options.body === 'string'
						? JSON.parse(options.body)
						: options.body;

				return handleMockFallback(
					endpoint,
					options.method || 'GET',
					body
				);
			} catch (mockErr) {
				console.error(
					'[API] Fallback handler failed:',
					mockErr
				);

				throw new Error(
					mockErr.message ||
					'Network connection failed and mock response was unavailable.'
				);
			}
		}

		throw new Error(
			'Could not connect to backend server at ' + API_BASE_URL
		);
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
			throw new Error(
				`Request failed with status ${response.status} (${response.statusText})`
			);
		}

		return null;
	}

	if (!response.ok || (json && json.success === false)) {
		const msg =
			json?.message ||
			`Request failed with status ${response.status}`;

		throw new Error(msg);
	}

	// Supports { success: true, message: "...", data: {...} }
	return json.data !== undefined ? json.data : json;
}

/* =========================================
   API METHODS
========================================= */

export const api = {
	get: (endpoint, options = {}) =>
		request(endpoint, { ...options, method: 'GET' }),

	post: (endpoint, body, options = {}) =>
		request(endpoint, {
			...options,
			method: 'POST',
			body:
				body instanceof FormData
					? body
					: JSON.stringify(body)
		}),

	patch: (endpoint, body, options = {}) =>
		request(endpoint, {
			...options,
			method: 'PATCH',
			body: body
				? body instanceof FormData
					? body
					: JSON.stringify(body)
				: undefined
		}),

	delete: (endpoint, options = {}) =>
		request(endpoint, {
			...options,
			method: 'DELETE'
		}),

	// PROFILE: localStorage demo methods
	profile: {
		get: (user) => getLocalProfile(user),

		update: (profileData, user) =>
			saveLocalProfile(profileData, user)
	},

	// AUTH
	auth: {
		login: (credentials) =>
			api.post('/api/v1/auth/login', credentials),

		register: (userData) =>
			api.post('/api/v1/auth/register', userData)
	},

	// TASKS
	tasks: {
		list: () => api.get('/api/v1/tasks'),

		mine: () => api.get('/api/v1/tasks/mine'),
		get: (id) => api.get(`/api/v1/tasks/${id}`),

		create: (task) => api.post('/api/v1/tasks', task),

		assign: (id, youthId) =>
			api.patch(
				`/api/v1/tasks/${id}/assign?youthId=${encodeURIComponent(youthId)}`
			),

		complete: (id) =>
			api.patch(`/api/v1/tasks/${id}/complete`)
	},

	// EXPERIENCES
	experiences: {
		get: (id) =>
			api.get(`/api/v1/experiences/${id}`),

		listForYouth: (youthId) =>
			api.get(`/api/v1/experiences/youth/${youthId}`),

		verify: (id, feedback = '', rating = 5) => {
			const query = new URLSearchParams({
				feedback: feedback || '',
				rating: String(rating)
			});

			return api.patch(
				`/api/v1/experiences/${id}/verify?${query.toString()}`
			);
		}
	}
};

export default api;
