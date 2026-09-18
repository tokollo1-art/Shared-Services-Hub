<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import { currentUser } from '$lib/stores/auth.js';

	const user = $derived($currentUser);
</script>

<svelte:head>
	<title>System Admin Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<div class="rounded-3xl border border-slate-200 bg-slate-900 p-8 text-white shadow-xl mb-8">
			<div class="flex items-center justify-between">
				<div>
					<div class="inline-flex items-center gap-1.5 rounded-full bg-purple-500/20 px-3 py-1 text-xs font-bold text-purple-300 border border-purple-400/30 mb-2">
						<span>⚡ System Administrator</span>
					</div>
					<h1 class="text-3xl font-extrabold tracking-tight">
						Platform Operations & Governance
					</h1>
					<p class="mt-1 text-sm text-slate-400 max-w-xl">
						Manage platform users, monitor matching health, and audit Experience Ledger integrity.
					</p>
				</div>
			</div>

			<div class="mt-8 pt-6 border-t border-slate-800 grid grid-cols-2 sm:grid-cols-4 gap-6">
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase">Total Users</div>
					<div class="text-3xl font-black mt-1">1,240</div>
				</div>
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase">Active Tasks</div>
					<div class="text-3xl font-black mt-1 text-indigo-400">184</div>
				</div>
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase">Verified Records</div>
					<div class="text-3xl font-black mt-1 text-purple-400">892</div>
				</div>
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase">Disbursed Funds</div>
					<div class="text-3xl font-black mt-1 text-emerald-400">R 1.2M</div>
				</div>
			</div>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-3 gap-6">
			<a
				href="/tasks"
				class="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-indigo-400 hover:shadow-md transition-all group"
			>
				<h3 class="font-bold text-slate-900 group-hover:text-indigo-600">Task Directory</h3>
				<p class="mt-1 text-xs text-slate-500">Inspect and monitor all active and completed tasks on the platform.</p>
			</a>
			<a
				href="/dashboard/youth"
				class="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-indigo-400 hover:shadow-md transition-all group"
			>
				<h3 class="font-bold text-slate-900 group-hover:text-indigo-600">Candidate View</h3>
				<p class="mt-1 text-xs text-slate-500">Preview experience ledger and open task matching from the youth perspective.</p>
			</a>
			<a
				href="/dashboard/sme"
				class="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-indigo-400 hover:shadow-md transition-all group"
			>
				<h3 class="font-bold text-slate-900 group-hover:text-indigo-600">SME Partner View</h3>
				<p class="mt-1 text-xs text-slate-500">Test task publishing, assignment flows, and experience verification.</p>
			</a>
		</div>
	</div>
</RoleGuard>
