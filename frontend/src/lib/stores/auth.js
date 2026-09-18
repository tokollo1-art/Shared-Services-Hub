import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';
import { goto } from '$app/navigation';

export const TOKEN_KEY = 'ssh_token';
export const USER_KEY = 'ssh_user';

/**
 * Returns dashboard path according to user role
 * @param {string} role
 * @returns {string}
 */
export function getRoleDashboard(role) {
	switch (role?.toUpperCase()) {
		case 'YOUTH':
			return '/dashboard/youth';
		case 'SME':
			return '/dashboard/sme';
		case 'CORPORATE':
			return '/dashboard/corporate';
		case 'ADMIN':
			return '/dashboard/admin';
		default:
			return '/tasks';
	}
}

function createAuthStore() {
	const initial = {
		token: null,
		user: null,
		initialized: false
	};

	const { subscribe, set, update } = writable(initial);

	/**
	 * Initializes the store from localStorage on client-side
	 */
	function init() {
		if (!browser) return;

		try {
			const token = localStorage.getItem(TOKEN_KEY);
			const userStr = localStorage.getItem(USER_KEY);
			const user = userStr ? JSON.parse(userStr) : null;

			if (token && user) {
				set({ token, user, initialized: true });
			} else {
				set({ token: null, user: null, initialized: true });
			}
		} catch (err) {
			console.error('Error rehydrating auth store:', err);
			set({ token: null, user: null, initialized: true });
		}
	}

	/**
	 * Sets token and user, stores in localStorage
	 * @param {{ token: string, user: any }} authResponse
	 * @param {boolean} shouldRedirect
	 */
	function login(authResponse, shouldRedirect = true) {
		const token = authResponse.token;
		const user = authResponse.user;

		if (browser) {
			localStorage.setItem(TOKEN_KEY, token);
			localStorage.setItem(USER_KEY, JSON.stringify(user));
		}

		set({ token, user, initialized: true });

		if (shouldRedirect && user?.role) {
			const target = getRoleDashboard(user.role);
			goto(target);
		}
	}

	/**
	 * Clears auth state, wipes localStorage, redirects to /login
	 */
	function logout() {
		if (browser) {
			localStorage.removeItem(TOKEN_KEY);
			localStorage.removeItem(USER_KEY);
		}

		set({ token: null, user: null, initialized: true });

		if (browser) {
			goto('/login');
		}
	}

	/**
	 * Updates the current user profile in store and localStorage
	 * @param {any} updatedUser
	 */
	function updateUser(updatedUser) {
		update((state) => {
			const newUser = { ...state.user, ...updatedUser };
			if (browser) {
				localStorage.setItem(USER_KEY, JSON.stringify(newUser));
			}
			return { ...state, user: newUser };
		});
	}

	return {
		subscribe,
		init,
		login,
		logout,
		updateUser,
		getRoleDashboard
	};
}

export const auth = createAuthStore();

export const isAuthenticated = derived(auth, ($auth) => Boolean($auth.token && $auth.user));
export const currentUser = derived(auth, ($auth) => $auth.user);
export const userRole = derived(auth, ($auth) => $auth.user?.role || null);
